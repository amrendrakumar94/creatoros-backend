package com.creatoros.controller;

import com.creatoros.dto.invoice.CreateInvoiceRequest;
import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.SendReminderRequest;
import com.creatoros.dto.invoice.UpdateInvoiceStatusRequest;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.InvoiceOverdueService;
import com.creatoros.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceOverdueService invoiceOverdueService;

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> list() {
        return ResponseEntity.ok(invoiceService.listForCreator(SecurityUtils.currentCreatorId()));
    }

    /** Forces the overdue check for this creator instead of waiting for the nightly sweep. */
    @PostMapping("/refresh-overdue")
    public ResponseEntity<Map<String, Integer>> refreshOverdue() {
        int updated = invoiceOverdueService.refreshForCreator(SecurityUtils.currentCreatorId());
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.get(SecurityUtils.currentCreatorId(), id));
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> create(@Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.create(SecurityUtils.currentCreatorId(), request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceDto> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateInvoiceStatusRequest request) {
        return ResponseEntity.ok(
                invoiceService.updateStatus(SecurityUtils.currentCreatorId(), id, request));
    }

    @PatchMapping("/{id}/paid")
    public ResponseEntity<InvoiceDto> markPaid(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidDate) {
        return ResponseEntity.ok(invoiceService.markPaid(SecurityUtils.currentCreatorId(), id, paidDate));
    }

    @PostMapping("/{id}/reminder")
    public ResponseEntity<InvoiceDto> sendReminder(@PathVariable Long id,
                                                   @Valid @RequestBody(required = false) SendReminderRequest request) {
        return ResponseEntity.ok(
                invoiceService.sendReminder(SecurityUtils.currentCreatorId(), id, request));
    }

    @PatchMapping("/{id}/expected-settlement")
    public ResponseEntity<InvoiceDto> setExpectedSettlement(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedDate) {
        return ResponseEntity.ok(
                invoiceService.setExpectedSettlementDate(SecurityUtils.currentCreatorId(), id, expectedDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        invoiceService.delete(SecurityUtils.currentCreatorId(), id);
        return ResponseEntity.noContent().build();
    }
}
