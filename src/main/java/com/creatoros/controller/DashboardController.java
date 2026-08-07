package com.creatoros.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creatoros.dto.dashboard.DashboardDto;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDto> summary(@RequestParam(required = false) String financialYear) {
        return ResponseEntity.ok(dashboardService.summary(SecurityUtils.currentCreatorId(), financialYear));
    }
}
