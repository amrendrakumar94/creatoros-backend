package com.creatoros.serviceimpl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.creatoros.dao.DocumentCounterDao;
import com.creatoros.enums.DocumentType;
import com.creatoros.service.DocumentNumber;
import com.creatoros.service.DocumentNumberService;
import com.creatoros.util.FinancialYear;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentNumberServiceImpl implements DocumentNumberService {

    private final DocumentCounterDao documentCounterDao;

    @Override
    public String nextDealNumber(Long creatorId, LocalDate on) {
        int sequence = documentCounterDao.nextSequence(creatorId, DocumentType.BRAND_DEAL, FinancialYear.labelOf(on));
        return "BD-%d-%02d".formatted(FinancialYear.startYearOf(on), sequence);
    }

    @Override
    public DocumentNumber nextInvoiceNumber(Long creatorId, LocalDate on) {
        String financialYear = FinancialYear.labelOf(on);
        int sequence = documentCounterDao.nextSequence(creatorId, DocumentType.INVOICE, financialYear);
        return new DocumentNumber("INV/%s/%03d".formatted(financialYear, sequence), sequence, financialYear);
    }
}
