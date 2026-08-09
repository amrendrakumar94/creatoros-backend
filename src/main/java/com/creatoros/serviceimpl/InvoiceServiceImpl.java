package com.creatoros.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.BrandDealDao;
import com.creatoros.dao.CreatorDao;
import com.creatoros.dao.InvoiceDao;
import com.creatoros.dao.InvoicePaymentDao;
import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceLineItemDto;
import com.creatoros.dto.invoice.InvoiceRequest;
import com.creatoros.dto.invoice.RecordPaymentRequest;
import com.creatoros.entity.BrandDeal;
import com.creatoros.entity.BuyerSnapshot;
import com.creatoros.entity.Creator;
import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoiceLineItem;
import com.creatoros.entity.InvoicePayment;
import com.creatoros.entity.SupplierSnapshot;
import com.creatoros.enums.InvoiceStatus;
import com.creatoros.enums.PaymentTerms;
import com.creatoros.enums.TdsSection;
import com.creatoros.enums.PermissionKey;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.service.DocumentNumber;
import com.creatoros.service.DocumentNumberService;
import com.creatoros.service.GstBreakdown;
import com.creatoros.service.GstCalculationService;
import com.creatoros.service.InvoiceService;
import com.creatoros.util.FinancialYear;
import com.creatoros.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final BigDecimal DEFAULT_GST_RATE = new BigDecimal("18.00");
    private static final String     DEFAULT_SAC_CODE = "998363";

    private final InvoiceDao              invoiceDao;
    private final InvoicePaymentDao       invoicePaymentDao;
    private final CreatorDao              creatorDao;
    private final BrandDealDao            brandDealDao;
    private final DocumentNumberService   documentNumberService;
    private final GstCalculationService   gstCalculationService;
    private final InvoiceMapper           invoiceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDto> listForCreator(Long creatorId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_INVOICES, PermissionKey.MANAGE_PAYMENTS, PermissionKey.VIEW_DASHBOARD);
        return invoiceDao.findByCreatorIdOrderByIssueDateDescIdDesc(creatorId).stream().map(invoiceMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto get(Long creatorId, Long invoiceId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_INVOICES, PermissionKey.MANAGE_PAYMENTS, PermissionKey.VIEW_DASHBOARD);
        return invoiceMapper.toDto(requireInvoice(creatorId, invoiceId));
    }

    @Override
    @Transactional
    public InvoiceDto create(Long creatorId, InvoiceRequest request) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_INVOICES);
        Creator creator = requireCreator(creatorId);
        LocalDate issueDate = request.issueDate() == null ? LocalDate.now() : request.issueDate();

        DocumentNumber number = documentNumberService.nextInvoiceNumber(creatorId, issueDate);

        Invoice invoice = Invoice.builder().creator(creator).status(InvoiceStatus.DRAFT).invoiceNumber(number.value())
                .financialYear(number.financialYear()).sequenceInYear(number.sequence()).issueDate(issueDate).dueDate(issueDate).build();

        applyRequest(invoice, creator, request, issueDate);
        invoiceDao.save(invoice);

        log.info("Created invoice {} for creator {}", invoice.getInvoiceNumber(), creatorId);
        return invoiceMapper.toDto(invoice);
    }

    @Override
    @Transactional
    public InvoiceDto update(Long creatorId, Long invoiceId, InvoiceRequest request) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_INVOICES);
        Invoice invoice = requireInvoice(creatorId, invoiceId);
        requireEditable(invoice);

        Creator creator = requireCreator(creatorId);
        LocalDate issueDate = request.issueDate() == null ? invoice.getIssueDate() : request.issueDate();

        if (!FinancialYear.labelOf(issueDate).equals(invoice.getFinancialYear())) {
            throw new BadRequestException(
                    "%s belongs to FY %s. Moving it to another financial year would break the invoice series - cancel it and raise a new one."
                            .formatted(invoice.getInvoiceNumber(), invoice.getFinancialYear()),
                    "ISSUE_DATE_CHANGES_FINANCIAL_YEAR");
        }

        applyRequest(invoice, creator, request, issueDate);
        invoice.setIssueDate(issueDate);

        return invoiceMapper.toDto(invoiceDao.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceDto updateStatus(Long creatorId, Long invoiceId, InvoiceStatus status) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_INVOICES);
        Invoice invoice = requireInvoice(creatorId, invoiceId);

        if (status == InvoiceStatus.PARTIALLY_PAID || status == InvoiceStatus.PAID) {
            throw new BadRequestException("Record a payment instead of setting this status directly.", "STATUS_DERIVED_FROM_PAYMENTS");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("This invoice has been cancelled and can no longer change.", "INVOICE_CANCELLED");
        }
        if (status == InvoiceStatus.DRAFT && invoice.getAmountPaid().signum() > 0) {
            throw new BadRequestException("Payments have already been recorded, so this invoice cannot go back to draft.", "INVOICE_HAS_PAYMENTS");
        }

        invoice.setStatus(status);
        invoiceDao.save(invoice);

        log.info("Invoice {} moved to {}", invoice.getInvoiceNumber(), status.getLabel());
        return invoiceMapper.toDto(invoice);
    }

    @Override
    @Transactional
    public InvoiceDto recordPayment(Long creatorId, Long invoiceId, RecordPaymentRequest request) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_PAYMENTS, PermissionKey.MANAGE_INVOICES);
        Invoice invoice = requireInvoice(creatorId, invoiceId);

        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new BadRequestException("Send the invoice before recording a payment against it.", "INVOICE_NOT_SENT");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("This invoice has been cancelled.", "INVOICE_CANCELLED");
        }

        BigDecimal tdsWithheld = request.tdsWithheld() == null ? BigDecimal.ZERO : request.tdsWithheld();
        BigDecimal settled = request.amount().add(tdsWithheld);
        if (settled.compareTo(invoice.getBalanceDue()) > 0) {
            throw new BadRequestException("That is more than the %s still outstanding on this invoice.".formatted(invoice.getBalanceDue()),
                    "PAYMENT_EXCEEDS_BALANCE");
        }

        InvoicePayment payment = InvoicePayment.builder().creator(invoice.getCreator()).invoice(invoice).amount(request.amount())
                .receivedOn(request.receivedOn() == null ? LocalDate.now() : request.receivedOn()).method(request.method())
                .reference(blankToNull(request.reference())).tdsWithheld(tdsWithheld).notes(blankToNull(request.notes())).build();

        invoicePaymentDao.save(payment);
        applySettlement(invoice);
        invoiceDao.save(invoice);

        log.info("Recorded {} against invoice {}", request.amount(), invoice.getInvoiceNumber());
        return invoiceMapper.toDto(invoice);
    }

    @Override
    @Transactional
    public void delete(Long creatorId, Long invoiceId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_INVOICES);
        Invoice invoice = requireInvoice(creatorId, invoiceId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BadRequestException("An issued invoice cannot be deleted. Cancel it instead so the numbering stays auditable.",
                    "ISSUED_INVOICE_IMMUTABLE");
        }

        invoiceDao.delete(invoice);
    }

    private void applyRequest(Invoice invoice, Creator creator, InvoiceRequest request, LocalDate issueDate) {
        invoice.setDeal(resolveDeal(creator.getId(), request.dealId()));
        invoice.setSupplier(toSupplierSnapshot(creator));
        invoice.setBuyer(toBuyerSnapshot(request));

        PaymentTerms terms = request.paymentTerms() == null ? PaymentTerms.NET_30 : request.paymentTerms();
        invoice.setPaymentTerms(terms);
        invoice.setDueDate(issueDate.plusDays(terms.getNetDays()));

        String placeOfSupplyCode = firstNonBlank(request.buyerStateCode(), creator.getStateCode());
        invoice.setPlaceOfSupplyCode(placeOfSupplyCode);
        invoice.setPlaceOfSupplyState(gstCalculationService.resolvePlaceOfSupplyName(placeOfSupplyCode));
        invoice.setInterState(gstCalculationService.isInterState(creator.getStateCode(), placeOfSupplyCode));
        invoice.setReverseCharge(Boolean.TRUE.equals(request.reverseCharge()));

        invoice.setNotes(blankToNull(request.notes()));
        invoice.setTerms(blankToNull(request.terms()));

        BigDecimal gstRate = resolveGstRate(creator, request);
        replaceLineItems(invoice, request.lineItems(), gstRate);
        recalculateTotals(invoice, request, gstRate);
        applySettlement(invoice);
    }

    private BigDecimal resolveGstRate(Creator creator, InvoiceRequest request) {
        if (!creator.isGstRegistered()) {
            return BigDecimal.ZERO;
        }
        if (creator.getStateCode() == null || creator.getStateCode().isBlank()) {
            throw new BadRequestException("Set your state of registration in Settings before raising a tax invoice.", "SUPPLIER_STATE_MISSING");
        }
        return request.gstRate() == null ? DEFAULT_GST_RATE : request.gstRate();
    }

    private void replaceLineItems(Invoice invoice, List<InvoiceLineItemDto> requested, BigDecimal gstRate) {
        invoice.getLineItems().clear();

        int order = 0;
        for (InvoiceLineItemDto dto : requested) {
            BigDecimal quantity = dto.quantity();
            BigDecimal taxable = quantity.multiply(dto.rate()).setScale(2, RoundingMode.HALF_UP);

            invoice.addLineItem(InvoiceLineItem.builder().description(dto.description().trim())
                    .sacCode(blankToNull(dto.sacCode()) == null ? DEFAULT_SAC_CODE : dto.sacCode().trim()).quantity(quantity)
                    .unit(blankToNull(dto.unit())).rate(dto.rate()).gstRate(gstRate).taxableAmount(taxable).sortOrder(order++).build());
        }
    }

    private void recalculateTotals(Invoice invoice, InvoiceRequest request, BigDecimal gstRate) {
        BigDecimal subtotal = invoice.getLineItems().stream().map(InvoiceLineItem::getTaxableAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = request.discountAmount() == null ? BigDecimal.ZERO : request.discountAmount().setScale(2, RoundingMode.HALF_UP);
        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("The discount is larger than the invoice subtotal.", "DISCOUNT_EXCEEDS_SUBTOTAL");
        }

        BigDecimal taxableValue = subtotal.subtract(discount);
        GstBreakdown gst = gstCalculationService.splitGst(taxableValue, gstRate, invoice.isInterState());

        TdsSection tdsSection = request.tdsSection() == null ? TdsSection.NONE : request.tdsSection();
        BigDecimal tdsAmount = gstCalculationService.calculateTds(taxableValue, tdsSection);
        BigDecimal invoiceTotal = taxableValue.add(gst.totalTax());

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discount);
        invoice.setCgstRate(gst.cgstRate());
        invoice.setCgstAmount(gst.cgstAmount());
        invoice.setSgstRate(gst.sgstRate());
        invoice.setSgstAmount(gst.sgstAmount());
        invoice.setIgstRate(gst.igstRate());
        invoice.setIgstAmount(gst.igstAmount());
        invoice.setTotalTax(gst.totalTax());
        invoice.setInvoiceTotal(invoiceTotal);
        invoice.setTdsSection(tdsSection);
        invoice.setTdsRate(tdsSection.getRate());
        invoice.setTdsAmount(tdsAmount);
        invoice.setNetReceivable(invoiceTotal.subtract(tdsAmount));
    }

    private void applySettlement(Invoice invoice) {
        List<InvoicePayment> payments = invoice.getId() == null ? List.of()
                : invoicePaymentDao.findByInvoiceIdOrderByReceivedOnAscIdAsc(invoice.getId());

        BigDecimal cash = payments.stream().map(InvoicePayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal withheld = payments.stream().map(InvoicePayment::getTdsWithheld).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal settled = cash.add(withheld);

        invoice.setAmountPaid(cash.setScale(2, RoundingMode.HALF_UP));
        invoice.setBalanceDue(invoice.getInvoiceTotal().subtract(settled).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));

        if (invoice.getStatus() == InvoiceStatus.DRAFT || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            return;
        }
        if (invoice.getBalanceDue().signum() == 0 && invoice.getInvoiceTotal().signum() > 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (settled.signum() > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        } else {
            invoice.setStatus(InvoiceStatus.SENT);
        }
    }

    private SupplierSnapshot toSupplierSnapshot(Creator creator) {
        String legalName = firstNonBlank(creator.getTradeName(), creator.getName());
        return SupplierSnapshot.builder().legalName(legalName).gstin(creator.getGstin()).pan(creator.getPan()).address(creator.getAddress())
                .city(creator.getCity()).state(creator.getState()).stateCode(creator.getStateCode()).pincode(creator.getPincode()).build();
    }

    private BuyerSnapshot toBuyerSnapshot(InvoiceRequest request) {
        String stateCode = blankToNull(request.buyerStateCode());
        return BuyerSnapshot.builder().name(request.buyerName().trim()).legalName(blankToNull(request.buyerLegalName()))
                .gstin(upperOrNull(request.buyerGstin())).email(blankToNull(request.buyerEmail())).address(blankToNull(request.buyerAddress()))
                .city(blankToNull(request.buyerCity())).state(gstCalculationService.resolvePlaceOfSupplyName(stateCode)).stateCode(stateCode)
                .pincode(blankToNull(request.buyerPincode())).build();
    }

    private BrandDeal resolveDeal(Long creatorId, String dealId) {
        if (dealId == null || dealId.isBlank()) {
            return null;
        }
        long parsed;
        try {
            parsed = Long.parseLong(dealId.trim());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Unrecognised deal reference.", "INVALID_DEAL_ID");
        }
        return brandDealDao.findByIdAndCreatorId(parsed, creatorId).orElseThrow(() -> ResourceNotFoundException.of("Brand deal", parsed));
    }

    private void requireEditable(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("This invoice has been cancelled and can no longer be edited.", "INVOICE_CANCELLED");
        }
        if (invoice.getAmountPaid().signum() > 0) {
            throw new BadRequestException("Payments have been recorded against this invoice, so its amounts can no longer change.",
                    "INVOICE_HAS_PAYMENTS");
        }
    }

    private Invoice requireInvoice(Long creatorId, Long invoiceId) {
        return invoiceDao.findByIdAndCreatorId(invoiceId, creatorId).orElseThrow(() -> ResourceNotFoundException.of("Invoice", invoiceId));
    }

    private Creator requireCreator(Long creatorId) {
        return creatorDao.findById(creatorId).orElseThrow(() -> ResourceNotFoundException.of("Creator", creatorId));
    }

    private String firstNonBlank(String first, String second) {
        String candidate = blankToNull(first);
        return candidate != null ? candidate : blankToNull(second);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String upperOrNull(String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }
}
