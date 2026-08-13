package com.creatoros.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.creatoros.dto.quotation.QuotationDto;
import com.creatoros.dto.quotation.QuotationRequest;
import com.creatoros.dto.quotation.ScheduleQuotationSendRequest;
import com.creatoros.dto.quotation.SendQuotationRequest;
import com.creatoros.dto.quotation.UpdateQuotationStatusRequest;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.QuotationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<List<QuotationDto>> list() {
        return ResponseEntity.ok(quotationService.listForCreator(SecurityUtils.currentTenantId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotationDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(quotationService.get(SecurityUtils.currentTenantId(), id));
    }

    @PostMapping
    public ResponseEntity<QuotationDto> create(@Valid @RequestBody QuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quotationService.create(SecurityUtils.currentTenantId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuotationDto> update(@PathVariable Long id, @Valid @RequestBody QuotationRequest request) {
        return ResponseEntity.ok(quotationService.update(SecurityUtils.currentTenantId(), id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<QuotationDto> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateQuotationStatusRequest request) {
        return ResponseEntity.ok(quotationService.updateStatus(SecurityUtils.currentTenantId(), id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quotationService.delete(SecurityUtils.currentTenantId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<QuotationDto> send(@PathVariable Long id, @Valid @RequestBody SendQuotationRequest request) {
        return ResponseEntity.ok(quotationService.sendNow(SecurityUtils.currentTenantId(), id, request.toEmail()));
    }

    @PostMapping("/{id}/schedule-send")
    public ResponseEntity<QuotationDto> scheduleSend(@PathVariable Long id, @Valid @RequestBody ScheduleQuotationSendRequest request) {
        return ResponseEntity.ok(quotationService.scheduleSend(SecurityUtils.currentTenantId(), id, request.toEmail(), request.sendAt()));
    }

    @PostMapping("/{id}/schedule-send/cancel")
    public ResponseEntity<QuotationDto> cancelScheduledSend(@PathVariable Long id) {
        return ResponseEntity.ok(quotationService.cancelScheduledSend(SecurityUtils.currentTenantId(), id));
    }

    @GetMapping(value = "/{id}/document", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> document(@PathVariable Long id) {
        return ResponseEntity.ok(quotationService.getQuotationHtml(SecurityUtils.currentTenantId(), id));
    }

    @GetMapping(value = "/{id}/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] pdf = quotationService.getQuotationPdf(SecurityUtils.currentTenantId(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("quotation-" + id + ".pdf").build().toString())
                .body(pdf);
    }

    @PostMapping("/{id}/convert-to-invoice")
    public ResponseEntity<QuotationDto> convertToInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(quotationService.convertToInvoice(SecurityUtils.currentTenantId(), id));
    }
}
