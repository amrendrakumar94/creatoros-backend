package com.creatoros.service;

import com.creatoros.dto.invoice.CreateInvoiceRequest;
import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceItemDto;
import com.creatoros.dto.invoice.SendReminderRequest;
import com.creatoros.dto.invoice.UpdateInvoiceStatusRequest;
import com.creatoros.entity.BankDetails;
import com.creatoros.entity.BrandDeal;
import com.creatoros.entity.Creator;
import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoiceBankSnapshot;
import com.creatoros.entity.InvoiceItem;
import com.creatoros.entity.InvoiceStatus;
import com.creatoros.entity.NotificationType;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.repository.BrandDealRepository;
import com.creatoros.repository.CreatorRepository;
import com.creatoros.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final int DEFAULT_PAYMENT_DAYS = 30;

    private final InvoiceRepository invoiceRepository;
    private final BrandDealRepository brandDealRepository;
    private final CreatorRepository creatorRepository;
    private final GstCalculationService gstCalculationService;
    private final NotificationService notificationService;
    private final InvoiceOverdueService invoiceOverdueService;
    private final DomainMapper domainMapper;

    /**
     * Not read-only: past-due invoices are flipped to Overdue before the list is returned, so the
     * UI never shows a stale status while waiting for the nightly sweep.
     */
    @Override
    @Transactional
    public List<InvoiceDto> listForCreator(Long creatorId) {
        invoiceOverdueService.refreshForCreator(creatorId);
        return invoiceRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId).stream()
                .map(domainMapper::toInvoiceDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto get(Long creatorId, Long invoiceId) {
        return domainMapper.toInvoiceDto(requireInvoice(creatorId, invoiceId));
    }

    @Override
    @Transactional
    public InvoiceDto create(Long creatorId, CreateInvoiceRequest request) {
        Creator creator = requireCreator(creatorId);

        LocalDate issueDate = request.issueDate() == null ? LocalDate.now() : request.issueDate();
        LocalDate dueDate = request.dueDate() == null
                ? issueDate.plusDays(DEFAULT_PAYMENT_DAYS)
                : request.dueDate();

        if (dueDate.isBefore(issueDate)) {
            throw new BadRequestException("Due date cannot be before the issue date", "INVALID_DUE_DATE");
        }

        Invoice invoice = Invoice.builder()
                .creator(creator)
                .invoiceNumber(nextInvoiceNumber(creatorId))
                .brandName(request.brandName())
                .brandGstin(upperOrNull(request.brandGstin()))
                .brandAddress(blankToNull(request.brandAddress()))
                // Snapshot the creator's identity and payout details as they stand right now.
                .creatorName(firstNonBlank(creator.getTradeName(), creator.getName()))
                .creatorGstin(creator.getGstin())
                .creatorPan(creator.getPan())
                .bankDetails(snapshotBank(creator.getBankDetails()))
                .issueDate(issueDate)
                .dueDate(dueDate)
                .interstate(request.isInterstate())
                .status(request.status() == null ? InvoiceStatus.SENT : request.status())
                .reminderSentCount(0)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        int order = 0;
        for (InvoiceItemDto dto : request.items()) {
            int quantity = dto.quantity() == null || dto.quantity() < 1 ? 1 : dto.quantity();
            BigDecimal lineAmount = dto.unitPrice().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineAmount);

            invoice.addItem(InvoiceItem.builder()
                    .description(blankToNull(dto.description()))
                    .sacCode(blankToNull(dto.sacCode()))
                    .quantity(quantity)
                    .unitPrice(dto.unitPrice())
                    .amount(lineAmount)
                    .sortOrder(order++)
                    .build());
        }

        applyTax(invoice, subtotal);
        linkDeal(invoice, creatorId, request.dealId());

        invoiceRepository.save(invoice);

        // Keep the deal's back-reference in step, which the old client-side flow never did.
        if (invoice.getDealId() != null) {
            brandDealRepository.findByIdAndCreatorId(invoice.getDealId(), creatorId)
                    .ifPresent(deal -> {
                        deal.setInvoiceId(invoice.getId());
                        brandDealRepository.save(deal);
                    });
        }

        notificationService.record(creator, NotificationType.INVOICE,
                "Invoice %s created".formatted(invoice.getInvoiceNumber()),
                "Raised for %s. Net receivable after TDS: %s".formatted(
                        invoice.getBrandName(), invoice.getNetReceivable()),
                "invoices", invoice.getNetReceivable());

        log.info("Created invoice {} for creator {}", invoice.getInvoiceNumber(), creatorId);
        return domainMapper.toInvoiceDto(invoice);
    }

    @Override
    @Transactional
    public InvoiceDto updateStatus(Long creatorId, Long invoiceId, UpdateInvoiceStatusRequest request) {
        if (request.status() == InvoiceStatus.PAID) {
            return markPaid(creatorId, invoiceId, request.paidDate());
        }

        Invoice invoice = requireInvoice(creatorId, invoiceId);
        invoice.setStatus(request.status());
        if (request.status() != InvoiceStatus.PAID) {
            invoice.setPaidDate(null);
        }
        return domainMapper.toInvoiceDto(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceDto markPaid(Long creatorId, Long invoiceId, LocalDate paidDate) {
        Invoice invoice = requireInvoice(creatorId, invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return domainMapper.toInvoiceDto(invoice);
        }

        LocalDate settled = paidDate == null ? LocalDate.now() : paidDate;
        if (settled.isBefore(invoice.getIssueDate())) {
            throw new BadRequestException("Payment date cannot be before the invoice issue date",
                    "INVALID_PAID_DATE");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(settled);
        invoiceRepository.save(invoice);

        notificationService.record(invoice.getCreator(), NotificationType.PAYMENT,
                "Payment received: %s".formatted(invoice.getBrandName()),
                "Invoice %s settled.".formatted(invoice.getInvoiceNumber()),
                "payments", invoice.getNetReceivable());

        return domainMapper.toInvoiceDto(invoice);
    }

    @Override
    @Transactional
    public InvoiceDto sendReminder(Long creatorId, Long invoiceId, SendReminderRequest request) {
        Invoice invoice = requireInvoice(creatorId, invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("This invoice is already paid", "INVOICE_ALREADY_PAID");
        }

        invoice.setReminderSentCount(invoice.getReminderSentCount() + 1);
        invoice.setLastReminderDate(LocalDate.now());
        invoiceRepository.save(invoice);

        String channel = request == null || request.channel() == null ? "email" : request.channel();

        // Actual WhatsApp/email delivery needs a messaging provider; the follow-up is recorded
        // so the collection history stays accurate either way.
        log.info("Reminder #{} logged for invoice {} via {}",
                invoice.getReminderSentCount(), invoice.getInvoiceNumber(), channel);

        notificationService.record(invoice.getCreator(), NotificationType.PAYMENT,
                "Reminder sent to %s".formatted(invoice.getBrandName()),
                "Follow-up #%d for invoice %s via %s.".formatted(
                        invoice.getReminderSentCount(), invoice.getInvoiceNumber(), channel),
                "payments", invoice.getNetReceivable());

        return domainMapper.toInvoiceDto(invoice);
    }

    @Override
    @Transactional
    public InvoiceDto setExpectedSettlementDate(Long creatorId, Long invoiceId, LocalDate expectedDate) {
        Invoice invoice = requireInvoice(creatorId, invoiceId);
        invoice.setExpectedSettlementDate(expectedDate);
        return domainMapper.toInvoiceDto(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public void delete(Long creatorId, Long invoiceId) {
        Invoice invoice = requireInvoice(creatorId, invoiceId);

        // Clear the deal's back-reference first, or it would dangle.
        if (invoice.getDealId() != null) {
            brandDealRepository.findByIdAndCreatorId(invoice.getDealId(), creatorId)
                    .ifPresent(deal -> {
                        deal.setInvoiceId(null);
                        brandDealRepository.save(deal);
                    });
        }
        invoiceRepository.delete(invoice);
    }

    private void applyTax(Invoice invoice, BigDecimal subtotal) {
        GstCalculationService.GstBreakdown tax =
                gstCalculationService.calculate(subtotal, invoice.isInterstate());

        invoice.setSubtotal(tax.subtotal());
        invoice.setCgstAmount(tax.cgst());
        invoice.setSgstAmount(tax.sgst());
        invoice.setIgstAmount(tax.igst());
        invoice.setTotalGst(tax.totalGst());
        invoice.setTdsDeducted(tax.tdsDeducted());
        invoice.setTotalAmount(tax.totalAmount());
        invoice.setNetReceivable(tax.netReceivable());
    }

    private void linkDeal(Invoice invoice, Long creatorId, String dealId) {
        if (dealId == null || dealId.isBlank()) {
            return;
        }
        long parsed;
        try {
            parsed = Long.parseLong(dealId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid deal reference: " + dealId, "INVALID_DEAL_ID");
        }
        BrandDeal deal = brandDealRepository.findByIdAndCreatorId(parsed, creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Brand deal", dealId));
        invoice.setDealId(deal.getId());
    }

    private InvoiceBankSnapshot snapshotBank(BankDetails bank) {
        if (bank == null) {
            return new InvoiceBankSnapshot();
        }
        return InvoiceBankSnapshot.builder()
                .bankName(bank.getBankName())
                .accountNumber(bank.getAccountNumber())
                .ifscCode(bank.getIfscCode())
                .upiId(bank.getUpiId())
                .build();
    }

    /** COS-YYYY-NNN, sequential per creator and unique by database constraint. */
    private String nextInvoiceNumber(Long creatorId) {
        long next = invoiceRepository.countByCreatorId(creatorId) + 1;
        return "COS-%d-%03d".formatted(LocalDate.now().getYear(), next);
    }

    private Invoice requireInvoice(Long creatorId, Long invoiceId) {
        return invoiceRepository.findByIdAndCreatorId(invoiceId, creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Invoice", invoiceId));
    }

    private Creator requireCreator(Long creatorId) {
        return creatorRepository.findById(creatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Creator", creatorId));
    }

    private String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String upperOrNull(String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }
}
