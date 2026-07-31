package com.creatoros.controller;

import com.creatoros.dto.expense.ExpenseDto;
import com.creatoros.dto.expense.ExpenseRequest;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> list() {
        return ResponseEntity.ok(expenseService.listForCreator(SecurityUtils.currentCreatorId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.get(SecurityUtils.currentCreatorId(), id));
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> create(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.create(SecurityUtils.currentCreatorId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> update(@PathVariable Long id,
                                             @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.update(SecurityUtils.currentCreatorId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(SecurityUtils.currentCreatorId(), id);
        return ResponseEntity.noContent().build();
    }
}
