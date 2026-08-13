package com.creatoros.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatoros.dao.BrandDealDao;
import com.creatoros.dao.CreatorDao;
import com.creatoros.dao.QuotationDao;
import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceLineItemDto;
import com.creatoros.dto.invoice.InvoiceRequest;
import com.creatoros.dto.quotation.QuotationDto;
import com.creatoros.dto.quotation.QuotationLineItemDto;
import com.creatoros.dto.quotation.QuotationRequest;
import com.creatoros.entity.BrandDeal;
import com.creatoros.entity.BuyerSnapshot;
import com.creatoros.entity.Creator;
import com.creatoros.entity.Quotation;
import com.creatoros.entity.QuotationLineItem;
import com.creatoros.entity.SupplierSnapshot;
import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.QuotationStatus;
import com.creatoros.enums.TdsSection;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.DocumentNumber;
import com.creatoros.service.DocumentNumberService;
import com.creatoros.service.GstBreakdown;
import com.creatoros.service.GstCalculationService;
import com.creatoros.service.InvoiceService;
import com.creatoros.service.QuotationService;
import com.creatoros.util.EmailService;
import com.creatoros.util.FinancialYear;
import com.creatoros.util.QuotationDocumentRenderer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private static final BigDecimal DEFAULT_GST_RATE = new BigDecimal("18.00");
    private static final String     DEFAULT_SAC_CODE = "998363";

    private final QuotationDao              quotationDao;
    private final CreatorDao                creatorDao;
    private final BrandDealDao              brandDealDao;
    private final DocumentNumberService     documentNumberService;
    private final GstCalculationService     gstCalculationService;
    private final QuotationMapper           quotationMapper;
    private final EmailService              emailService;
    private final QuotationDocumentRenderer documentRenderer;
    private final InvoiceService            invoiceService;

    @Override
    @Transactional
    public List<QuotationDto> listForCreator(Long creatorId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS, PermissionKey.VIEW_DASHBOARD);
        List<Quotation> quotations = quotationDao.findByCreatorIdOrderByIssueDateDescIdDesc(creatorId);
        quotations.forEach(this::applyExpiryAndPersist);
        return quotations.stream().map(quotationMapper::toDto).toList();
    }

    @Override
    @Transactional
    public QuotationDto get(Long creatorId, Long quotationId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS, PermissionKey.VIEW_DASHBOARD);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiryAndPersist(quotation);
        return quotationMapper.toDto(quotation);
    }

    @Override
    @Transactional
    public QuotationDto create(Long creatorId, QuotationRequest request) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS);
        Creator creator = requireCreator(creatorId);
        LocalDate issueDate = request.issueDate() == null ? LocalDate.now() : request.issueDate();

        DocumentNumber number = documentNumberService.nextQuotationNumber(creatorId, issueDate);

        Quotation quotation = Quotation.builder().creator(creator).status(QuotationStatus.DRAFT).quotationNumber(number.value())
                .financialYear(number.financialYear()).sequenceInYear(number.sequence()).issueDate(issueDate).validUntil(request.validUntil())
                .build();

        applyRequest(quotation, creator, request, issueDate);
        quotationDao.save(quotation);

        log.info("Created quotation {} for creator {}", quotation.getQuotationNumber(), creatorId);
        return quotationMapper.toDto(quotation);
    }

    @Override
    @Transactional
    public QuotationDto update(Long creatorId, Long quotationId, QuotationRequest request) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiry(quotation);
        requireEditable(quotation);

        Creator creator = requireCreator(creatorId);
        LocalDate issueDate = request.issueDate() == null ? quotation.getIssueDate() : request.issueDate();

        if (!FinancialYear.labelOf(issueDate).equals(quotation.getFinancialYear())) {
            throw new BadRequestException(
                    "%s belongs to FY %s. Moving it to another financial year would break the quotation series - cancel it and raise a new one."
                            .formatted(quotation.getQuotationNumber(), quotation.getFinancialYear()),
                    "ISSUE_DATE_CHANGES_FINANCIAL_YEAR");
        }

        applyRequest(quotation, creator, request, issueDate);
        quotation.setIssueDate(issueDate);
        applyExpiry(quotation);

        return quotationMapper.toDto(quotationDao.save(quotation));
    }

    @Override
    @Transactional
    public QuotationDto updateStatus(Long creatorId, Long quotationId, QuotationStatus status) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiry(quotation);

        if (status == QuotationStatus.EXPIRED) {
            throw new BadRequestException("Expiry is derived automatically from the validity date and cannot be set directly.",
                    "STATUS_DERIVED_FROM_VALIDITY");
        }
        if (quotation.getStatus().isFinal()) {
            throw new BadRequestException(
                    "This quotation is already %s and can no longer change status.".formatted(quotation.getStatus().getLabel().toLowerCase()),
                    "QUOTATION_FINALISED");
        }
        if ((status == QuotationStatus.ACCEPTED || status == QuotationStatus.REJECTED) && quotation.getStatus() != QuotationStatus.SENT) {
            throw new BadRequestException("Send the quotation to the client before recording their decision.", "QUOTATION_NOT_SENT");
        }

        quotation.setStatus(status);
        quotationDao.save(quotation);

        log.info("Quotation {} moved to {}", quotation.getQuotationNumber(), status.getLabel());
        return quotationMapper.toDto(quotation);
    }

    @Override
    @Transactional
    public void delete(Long creatorId, Long quotationId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiry(quotation);

        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new BadRequestException(
                    "An issued quotation cannot be deleted. Reject or let it expire instead so the numbering stays auditable.",
                    "ISSUED_QUOTATION_IMMUTABLE");
        }

        quotationDao.delete(quotation);
    }

    @Override
    @Transactional
    public QuotationDto sendNow(Long creatorId, Long quotationId, String toEmail) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiry(quotation);
        requireSendable(quotation);

        dispatchEmail(quotation, toEmail);
        if (quotation.getStatus() == QuotationStatus.DRAFT) {
            quotation.setStatus(QuotationStatus.SENT);
        }
        quotationDao.save(quotation);

        log.info("Sent quotation {} to {}", quotation.getQuotationNumber(), toEmail);
        return quotationMapper.toDto(quotation);
    }

    @Override
    @Transactional
    public QuotationDto scheduleSend(Long creatorId, Long quotationId, String toEmail, LocalDateTime sendAt) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiry(quotation);
        requireSendable(quotation);

        if (!sendAt.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Choose a time in the future to schedule this send.", "SCHEDULE_TIME_IN_PAST");
        }

        quotation.setScheduledSendAt(Timestamp.valueOf(sendAt));
        quotation.setScheduledSendEmail(toEmail);
        quotationDao.save(quotation);

        log.info("Scheduled quotation {} to send to {} at {}", quotation.getQuotationNumber(), toEmail, sendAt);
        return quotationMapper.toDto(quotation);
    }

    @Override
    @Transactional
    public QuotationDto cancelScheduledSend(Long creatorId, Long quotationId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiry(quotation);

        quotation.setScheduledSendAt(null);
        quotation.setScheduledSendEmail(null);
        quotationDao.save(quotation);

        return quotationMapper.toDto(quotation);
    }

    @Override
    @Transactional
    public String getQuotationHtml(Long creatorId, Long quotationId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS, PermissionKey.VIEW_DASHBOARD);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiryAndPersist(quotation);
        return documentRenderer.buildHtml(quotationMapper.toDto(quotation), quotation.getCreator());
    }

    @Override
    @Transactional
    public byte[] getQuotationPdf(Long creatorId, Long quotationId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS, PermissionKey.VIEW_DASHBOARD);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiryAndPersist(quotation);
        String html = documentRenderer.buildHtml(quotationMapper.toDto(quotation), quotation.getCreator());
        return documentRenderer.renderPdf(html);
    }

    @Override
    @Transactional
    public QuotationDto convertToInvoice(Long creatorId, Long quotationId) {
        SecurityUtils.requireAny(PermissionKey.MANAGE_QUOTATIONS);
        Quotation quotation = requireQuotation(creatorId, quotationId);
        applyExpiry(quotation);

        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            throw new BadRequestException("Only an accepted quotation can be converted to an invoice.", "QUOTATION_NOT_ACCEPTED");
        }
        if (quotation.getConvertedInvoiceId() != null) {
            throw new BadRequestException("This quotation has already been converted to an invoice.", "ALREADY_CONVERTED");
        }

        InvoiceDto invoiceDto = invoiceService.create(creatorId, toInvoiceRequest(quotation));
        quotation.setConvertedInvoiceId(Long.parseLong(invoiceDto.id()));
        quotationDao.save(quotation);

        log.info("Converted quotation {} to invoice {}", quotation.getQuotationNumber(), invoiceDto.invoiceNumber());
        return quotationMapper.toDto(quotation);
    }

    /**
     * Dispatches every quotation whose scheduled send has come due. Runs as a
     * system-wide batch job with no acting tenant, so - unlike every other
     * finder in this class - it deliberately queries across all creators rather
     * than being scoped to one.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void dispatchDueScheduledSends() {
        List<Quotation> due = quotationDao.findDueScheduledSends(new Timestamp(System.currentTimeMillis()));
        for (Quotation quotation : due) {
            try {
                dispatchEmail(quotation, quotation.getScheduledSendEmail());
                if (quotation.getStatus() == QuotationStatus.DRAFT) {
                    quotation.setStatus(QuotationStatus.SENT);
                }
                quotation.setScheduledSendAt(null);
                quotation.setScheduledSendEmail(null);
                quotationDao.save(quotation);
                log.info("Dispatched scheduled email for quotation {}", quotation.getQuotationNumber());
            } catch (Exception exception) {
                log.warn("Failed to dispatch scheduled email for quotation {}", quotation.getQuotationNumber(), exception);
            }
        }
    }

    private void applyExpiryAndPersist(Quotation quotation) {
        if (applyExpiry(quotation)) {
            quotationDao.save(quotation);
        }
    }

    /** Mutates only; returns true if the status changed so the caller can decide whether to persist. */
    private boolean applyExpiry(Quotation quotation) {
        if (quotation.getStatus() == QuotationStatus.SENT && quotation.getValidUntil() != null
                && quotation.getValidUntil().isBefore(LocalDate.now())) {
            quotation.setStatus(QuotationStatus.EXPIRED);
            return true;
        }
        return false;
    }

    private void requireSendable(Quotation quotation) {
        if (quotation.getStatus().isFinal()) {
            throw new BadRequestException(
                    "This quotation is %s and can no longer be sent.".formatted(quotation.getStatus().getLabel().toLowerCase()),
                    "QUOTATION_FINALISED");
        }
    }

    private void requireEditable(Quotation quotation) {
        if (!quotation.getStatus().isEditable()) {
            throw new BadRequestException(
                    "This quotation is %s and can no longer be edited.".formatted(quotation.getStatus().getLabel().toLowerCase()),
                    "QUOTATION_FINALISED");
        }
    }

    private void dispatchEmail(Quotation quotation, String toEmail) {
        QuotationDto dto = quotationMapper.toDto(quotation);
        String html = documentRenderer.buildHtml(dto, quotation.getCreator());
        String text = documentRenderer.buildPlainText(dto);
        byte[] pdf = documentRenderer.renderPdf(html);

        emailService.sendEmail(toEmail, "Quotation " + quotation.getQuotationNumber(), text, html,
                new EmailService.PdfAttachment(quotation.getQuotationNumber() + ".pdf", pdf));
        quotation.setLastEmailedAt(new Timestamp(System.currentTimeMillis()));
    }

    private void applyRequest(Quotation quotation, Creator creator, QuotationRequest request, LocalDate issueDate) {
        quotation.setDeal(resolveDeal(creator.getId(), request.dealId()));
        quotation.setSupplier(toSupplierSnapshot(creator));
        quotation.setBuyer(toBuyerSnapshot(request));
        quotation.setValidUntil(request.validUntil());

        String placeOfSupplyCode = firstNonBlank(request.buyerStateCode(), creator.getStateCode());
        quotation.setPlaceOfSupplyCode(placeOfSupplyCode);
        quotation.setPlaceOfSupplyState(gstCalculationService.resolvePlaceOfSupplyName(placeOfSupplyCode));
        quotation.setInterState(gstCalculationService.isInterState(creator.getStateCode(), placeOfSupplyCode));
        quotation.setReverseCharge(Boolean.TRUE.equals(request.reverseCharge()));

        quotation.setNotes(blankToNull(request.notes()));
        quotation.setTerms(blankToNull(request.terms()));

        BigDecimal gstRate = resolveGstRate(creator, request);
        replaceLineItems(quotation, request.lineItems(), gstRate);
        recalculateTotals(quotation, request, gstRate);
    }

    private BigDecimal resolveGstRate(Creator creator, QuotationRequest request) {
        if (!creator.isGstRegistered()) {
            return BigDecimal.ZERO;
        }
        if (creator.getStateCode() == null || creator.getStateCode().isBlank()) {
            throw new BadRequestException("Set your state of registration in Settings before raising a tax quotation.", "SUPPLIER_STATE_MISSING");
        }
        return request.gstRate() == null ? DEFAULT_GST_RATE : request.gstRate();
    }

    private void replaceLineItems(Quotation quotation, List<QuotationLineItemDto> requested, BigDecimal gstRate) {
        quotation.getLineItems().clear();

        int order = 0;
        for (QuotationLineItemDto dto : requested) {
            BigDecimal quantity = dto.quantity();
            BigDecimal taxable = quantity.multiply(dto.rate()).setScale(2, RoundingMode.HALF_UP);

            quotation.addLineItem(QuotationLineItem.builder().description(dto.description().trim())
                    .sacCode(blankToNull(dto.sacCode()) == null ? DEFAULT_SAC_CODE : dto.sacCode().trim()).quantity(quantity)
                    .unit(blankToNull(dto.unit())).rate(dto.rate()).gstRate(gstRate).taxableAmount(taxable).sortOrder(order++).build());
        }
    }

    private void recalculateTotals(Quotation quotation, QuotationRequest request, BigDecimal gstRate) {
        BigDecimal subtotal = quotation.getLineItems().stream().map(QuotationLineItem::getTaxableAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = request.discountAmount() == null ? BigDecimal.ZERO : request.discountAmount().setScale(2, RoundingMode.HALF_UP);
        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("The discount is larger than the quotation subtotal.", "DISCOUNT_EXCEEDS_SUBTOTAL");
        }

        BigDecimal taxableValue = subtotal.subtract(discount);
        GstBreakdown gst = gstCalculationService.splitGst(taxableValue, gstRate, quotation.isInterState());

        TdsSection tdsSection = request.tdsSection() == null ? TdsSection.NONE : request.tdsSection();
        BigDecimal tdsAmount = gstCalculationService.calculateTds(taxableValue, tdsSection);
        BigDecimal quotationTotal = taxableValue.add(gst.totalTax());

        quotation.setSubtotal(subtotal);
        quotation.setDiscountAmount(discount);
        quotation.setCgstRate(gst.cgstRate());
        quotation.setCgstAmount(gst.cgstAmount());
        quotation.setSgstRate(gst.sgstRate());
        quotation.setSgstAmount(gst.sgstAmount());
        quotation.setIgstRate(gst.igstRate());
        quotation.setIgstAmount(gst.igstAmount());
        quotation.setTotalTax(gst.totalTax());
        quotation.setQuotationTotal(quotationTotal);
        quotation.setTdsSection(tdsSection);
        quotation.setTdsRate(tdsSection.getRate());
        quotation.setTdsAmount(tdsAmount);
    }

    private SupplierSnapshot toSupplierSnapshot(Creator creator) {
        String legalName = firstNonBlank(creator.getTradeName(), creator.getName());
        return SupplierSnapshot.builder().legalName(legalName).gstin(creator.getGstin()).pan(creator.getPan()).address(creator.getAddress())
                .city(creator.getCity()).state(creator.getState()).stateCode(creator.getStateCode()).pincode(creator.getPincode()).build();
    }

    private BuyerSnapshot toBuyerSnapshot(QuotationRequest request) {
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

    private InvoiceRequest toInvoiceRequest(Quotation quotation) {
        BuyerSnapshot buyer = quotation.getBuyer();
        BigDecimal gstRate = quotation.getLineItems().isEmpty() ? null : quotation.getLineItems().get(0).getGstRate();

        List<InvoiceLineItemDto> lineItems = quotation.getLineItems().stream()
                .map(item -> new InvoiceLineItemDto(null, item.getDescription(), item.getSacCode(), item.getQuantity(), item.getUnit(),
                        item.getRate(), item.getGstRate(), null))
                .toList();

        return new InvoiceRequest(quotation.getDeal() == null ? null : String.valueOf(quotation.getDeal().getId()), buyer.getName(),
                buyer.getLegalName(), buyer.getGstin(), buyer.getEmail(), buyer.getAddress(), buyer.getCity(), buyer.getStateCode(),
                buyer.getPincode(), LocalDate.now(), null, gstRate, quotation.getDiscountAmount(), quotation.getTdsSection(),
                quotation.isReverseCharge(), quotation.getNotes(), null, lineItems);
    }

    private Quotation requireQuotation(Long creatorId, Long quotationId) {
        return quotationDao.findByIdAndCreatorId(quotationId, creatorId).orElseThrow(() -> ResourceNotFoundException.of("Quotation", quotationId));
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
