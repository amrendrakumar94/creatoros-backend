package com.creatoros.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceRequest;
import com.creatoros.dto.invoice.RecordPaymentRequest;
import com.creatoros.dto.invoice.UpdateInvoiceStatusRequest;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.InvoiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> list() {
        return ResponseEntity.ok(invoiceService.listForCreator(SecurityUtils.currentTenantId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.get(SecurityUtils.currentTenantId(), id));
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> create(@Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(SecurityUtils.currentTenantId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto> update(@PathVariable Long id, @Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.update(SecurityUtils.currentTenantId(), id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceDto> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateInvoiceStatusRequest request) {
        return ResponseEntity.ok(invoiceService.updateStatus(SecurityUtils.currentTenantId(), id, request.status()));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<InvoiceDto> recordPayment(@PathVariable Long id, @Valid @RequestBody RecordPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.recordPayment(SecurityUtils.currentTenantId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        invoiceService.delete(SecurityUtils.currentTenantId(), id);
        return ResponseEntity.noContent().build();
    }
}
