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

import com.creatoros.dto.deal.BrandDealDto;
import com.creatoros.dto.deal.BrandDealRequest;
import com.creatoros.dto.deal.UpdateDealStageRequest;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.BrandDealService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
public class BrandDealController {

    private final BrandDealService brandDealService;

    @GetMapping
    public ResponseEntity<List<BrandDealDto>> list() {
        return ResponseEntity.ok(brandDealService.listForCreator(SecurityUtils.currentTenantId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDealDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(brandDealService.get(SecurityUtils.currentTenantId(), id));
    }

    @PostMapping
    public ResponseEntity<BrandDealDto> create(@Valid @RequestBody BrandDealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandDealService.create(SecurityUtils.currentTenantId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDealDto> update(@PathVariable Long id, @Valid @RequestBody BrandDealRequest request) {
        return ResponseEntity.ok(brandDealService.update(SecurityUtils.currentTenantId(), id, request));
    }

    @PatchMapping("/{id}/stage")
    public ResponseEntity<BrandDealDto> updateStage(@PathVariable Long id, @Valid @RequestBody UpdateDealStageRequest request) {
        return ResponseEntity.ok(brandDealService.updateStage(SecurityUtils.currentTenantId(), id, request.stage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandDealService.delete(SecurityUtils.currentTenantId(), id);
        return ResponseEntity.noContent().build();
    }
}
