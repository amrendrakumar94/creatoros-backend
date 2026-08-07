package com.creatoros.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creatoros.dao.DocumentCounterDao;
import com.creatoros.enums.DocumentType;

@ExtendWith(MockitoExtension.class)
class DocumentNumberServiceImplTest {

    private static final Long          CREATOR = 42L;

    @Mock
    private DocumentCounterDao         documentCounterDao;

    @InjectMocks
    private DocumentNumberServiceImpl  service;

    @Test
    @DisplayName("keeps the existing BD-YYYY-NN deal format so old numbers stay consistent")
    void dealNumberKeepsLegacyFormat() {
        when(documentCounterDao.nextSequence(CREATOR, DocumentType.BRAND_DEAL, "2026-27")).thenReturn(4);

        assertThat(service.nextDealNumber(CREATOR, LocalDate.parse("2026-08-08"))).isEqualTo("BD-2026-04");
    }

    @Test
    @DisplayName("numbers a January deal against the financial year, not the calendar year")
    void dealNumberUsesFinancialYearNotCalendarYear() {
        when(documentCounterDao.nextSequence(CREATOR, DocumentType.BRAND_DEAL, "2026-27")).thenReturn(9);

        assertThat(service.nextDealNumber(CREATOR, LocalDate.parse("2027-01-15"))).isEqualTo("BD-2026-09");
    }

    @Test
    @DisplayName("resets the deal series when the financial year turns over on 1 April")
    void dealSeriesResetsOnNewFinancialYear() {
        when(documentCounterDao.nextSequence(CREATOR, DocumentType.BRAND_DEAL, "2027-28")).thenReturn(1);

        assertThat(service.nextDealNumber(CREATOR, LocalDate.parse("2027-04-01"))).isEqualTo("BD-2027-01");
    }

    @Test
    @DisplayName("issues a GST-legal invoice number inside the 16 character limit")
    void invoiceNumberFitsGstLimit() {
        when(documentCounterDao.nextSequence(CREATOR, DocumentType.INVOICE, "2026-27")).thenReturn(7);

        String number = service.nextInvoiceNumber(CREATOR, LocalDate.parse("2026-08-08"));

        assertThat(number).isEqualTo("INV/2026-27/007");
        assertThat(number).hasSizeLessThanOrEqualTo(16);
    }

    @Test
    @DisplayName("stays inside the 16 character limit past a thousand invoices")
    void invoiceNumberStaysLegalAtFourDigits() {
        when(documentCounterDao.nextSequence(CREATOR, DocumentType.INVOICE, "2026-27")).thenReturn(1234);

        assertThat(service.nextInvoiceNumber(CREATOR, LocalDate.parse("2026-08-08"))).isEqualTo("INV/2026-27/1234").hasSizeLessThanOrEqualTo(16);
    }

    @Test
    @DisplayName("keeps deal and invoice series independent of each other")
    void dealAndInvoiceSeriesAreIndependent() {
        when(documentCounterDao.nextSequence(CREATOR, DocumentType.BRAND_DEAL, "2026-27")).thenReturn(1);
        when(documentCounterDao.nextSequence(CREATOR, DocumentType.INVOICE, "2026-27")).thenReturn(1);

        LocalDate on = LocalDate.parse("2026-08-08");
        service.nextDealNumber(CREATOR, on);
        service.nextInvoiceNumber(CREATOR, on);

        verify(documentCounterDao).nextSequence(CREATOR, DocumentType.BRAND_DEAL, "2026-27");
        verify(documentCounterDao).nextSequence(CREATOR, DocumentType.INVOICE, "2026-27");
    }
}
