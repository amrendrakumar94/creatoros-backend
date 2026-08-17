package com.creatoros.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.creatoros.dao.BrandDealDao;
import com.creatoros.dao.CreatorDao;
import com.creatoros.dao.QuotationDao;
import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceRequest;
import com.creatoros.entity.BuyerSnapshot;
import com.creatoros.entity.Creator;
import com.creatoros.entity.Quotation;
import com.creatoros.entity.QuotationLineItem;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.InvoiceStatus;
import com.creatoros.enums.PaymentTerms;
import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.QuotationStatus;
import com.creatoros.enums.Role;
import com.creatoros.enums.TdsSection;
import com.creatoros.exception.BadRequestException;
import com.creatoros.security.CreatorPrincipal;
import com.creatoros.service.DocumentNumberService;
import com.creatoros.service.GstCalculationService;
import com.creatoros.service.InvoiceService;
import com.creatoros.util.EmailService;
import com.creatoros.util.QuotationDocumentRenderer;

@ExtendWith(MockitoExtension.class)
class QuotationServiceImplTest {

    private static final Long OWNER        = 1L;
    private static final Long QUOTATION_ID = 20L;

    @Mock
    private QuotationDao quotationDao;

    @Mock
    private CreatorDao creatorDao;

    @Mock
    private BrandDealDao brandDealDao;

    @Mock
    private DocumentNumberService documentNumberService;

    @Mock
    private GstCalculationService gstCalculationService;

    @Mock
    private QuotationMapper quotationMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private QuotationDocumentRenderer documentRenderer;

    @Mock
    private InvoiceService invoiceService;

    private QuotationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new QuotationServiceImpl(quotationDao, creatorDao, brandDealDao, documentNumberService, gstCalculationService, quotationMapper,
                emailService, documentRenderer, invoiceService);
        authenticateAsOwner(OWNER);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("sendNow dispatches an email and flips a draft quotation to Sent")
    void sendNowDispatchesEmailAndFlipsDraftToSent() {
        Quotation quotation = quotation(QuotationStatus.DRAFT);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));
        stubRendering();

        service.sendNow(OWNER, QUOTATION_ID, "brand@example.com");

        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.SENT);
        assertThat(quotation.getLastEmailedAt()).isNotNull();
        verify(emailService).sendEmail(eq("brand@example.com"), any(), any(), any(), any());
        verify(quotationDao).save(quotation);
    }

    @Test
    @DisplayName("sendNow rejects an already-final quotation")
    void sendNowRejectsFinalQuotation() {
        Quotation quotation = quotation(QuotationStatus.ACCEPTED);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.sendNow(OWNER, QUOTATION_ID, "brand@example.com")).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("QUOTATION_FINALISED"));
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("scheduleSend stores the recipient and time for a future send")
    void scheduleSendStoresFutureSend() {
        Quotation quotation = quotation(QuotationStatus.DRAFT);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        service.scheduleSend(OWNER, QUOTATION_ID, "brand@example.com", future);

        assertThat(quotation.getScheduledSendEmail()).isEqualTo("brand@example.com");
        assertThat(quotation.getScheduledSendAt()).isEqualTo(Timestamp.valueOf(future));
    }

    @Test
    @DisplayName("scheduleSend rejects a time in the past")
    void scheduleSendRejectsPastTime() {
        Quotation quotation = quotation(QuotationStatus.DRAFT);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.scheduleSend(OWNER, QUOTATION_ID, "brand@example.com", LocalDateTime.now().minusHours(1)))
                .isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("SCHEDULE_TIME_IN_PAST"));
    }

    @Test
    @DisplayName("cancelScheduledSend clears a pending schedule")
    void cancelScheduledSendClearsSchedule() {
        Quotation quotation = quotation(QuotationStatus.SENT);
        quotation.setScheduledSendAt(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        quotation.setScheduledSendEmail("brand@example.com");
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        service.cancelScheduledSend(OWNER, QUOTATION_ID);

        assertThat(quotation.getScheduledSendAt()).isNull();
        assertThat(quotation.getScheduledSendEmail()).isNull();
    }

    @Test
    @DisplayName("applyExpiry flips a Sent quotation past its validity date to Expired")
    void applyExpiryFlipsStaleSentToExpired() {
        Quotation quotation = quotation(QuotationStatus.SENT);
        quotation.setValidUntil(LocalDate.now().minusDays(1));

        boolean changed = ReflectionTestUtils.invokeMethod(service, "applyExpiry", quotation);

        assertThat(changed).isTrue();
        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.EXPIRED);
    }

    @Test
    @DisplayName("applyExpiry leaves a Sent quotation with a future validity date unchanged")
    void applyExpiryLeavesFutureValidityUnchanged() {
        Quotation quotation = quotation(QuotationStatus.SENT);
        quotation.setValidUntil(LocalDate.now().plusDays(1));

        boolean changed = ReflectionTestUtils.invokeMethod(service, "applyExpiry", quotation);

        assertThat(changed).isFalse();
        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.SENT);
    }

    @Test
    @DisplayName("applyExpiry leaves a Sent quotation with no validity date unchanged")
    void applyExpiryLeavesNoValidityUnchanged() {
        Quotation quotation = quotation(QuotationStatus.SENT);
        quotation.setValidUntil(null);

        boolean changed = ReflectionTestUtils.invokeMethod(service, "applyExpiry", quotation);

        assertThat(changed).isFalse();
        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.SENT);
    }

    @Test
    @DisplayName("applyExpiry is idempotent on an already-expired quotation")
    void applyExpiryIsIdempotentOnAlreadyExpired() {
        Quotation quotation = quotation(QuotationStatus.EXPIRED);
        quotation.setValidUntil(LocalDate.now().minusDays(30));

        boolean changed = ReflectionTestUtils.invokeMethod(service, "applyExpiry", quotation);

        assertThat(changed).isFalse();
        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.EXPIRED);
    }

    @Test
    @DisplayName("get persists an expiry flip discovered on read")
    void getPersistsExpiryFlipWhenStale() {
        Quotation quotation = quotation(QuotationStatus.SENT);
        quotation.setValidUntil(LocalDate.now().minusDays(1));
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        service.get(OWNER, QUOTATION_ID);

        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.EXPIRED);
        verify(quotationDao).save(quotation);
    }

    @Test
    @DisplayName("get does not write when nothing is stale")
    void getDoesNotPersistWhenNotStale() {
        Quotation quotation = quotation(QuotationStatus.DRAFT);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        service.get(OWNER, QUOTATION_ID);

        verify(quotationDao, never()).save(quotation);
    }

    @Test
    @DisplayName("listForCreator persists an expiry flip only for the stale rows")
    void listForCreatorPersistsExpiryFlipForStaleRowsOnly() {
        Quotation stale = quotation(QuotationStatus.SENT);
        stale.setId(11L);
        stale.setValidUntil(LocalDate.now().minusDays(1));

        Quotation fresh = quotation(QuotationStatus.SENT);
        fresh.setId(12L);
        fresh.setValidUntil(LocalDate.now().plusDays(5));

        when(quotationDao.findByCreatorIdOrderByIssueDateDescIdDesc(OWNER)).thenReturn(List.of(stale, fresh));

        service.listForCreator(OWNER);

        assertThat(stale.getStatus()).isEqualTo(QuotationStatus.EXPIRED);
        assertThat(fresh.getStatus()).isEqualTo(QuotationStatus.SENT);
        verify(quotationDao).save(stale);
        verify(quotationDao, never()).save(fresh);
    }

    @Test
    @DisplayName("updateStatus rejects setting Expired directly")
    void updateStatusRejectsDirectExpiredTarget() {
        Quotation quotation = quotation(QuotationStatus.SENT);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.updateStatus(OWNER, QUOTATION_ID, QuotationStatus.EXPIRED)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("STATUS_DERIVED_FROM_VALIDITY"));
    }

    @Test
    @DisplayName("updateStatus rejects any change once a quotation is final")
    void updateStatusRejectsChangeOnFinalQuotation() {
        Quotation quotation = quotation(QuotationStatus.ACCEPTED);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.updateStatus(OWNER, QUOTATION_ID, QuotationStatus.REJECTED)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("QUOTATION_FINALISED"));
    }

    @Test
    @DisplayName("updateStatus rejects recording a decision before the quotation was sent")
    void updateStatusRejectsAcceptedFromDraft() {
        Quotation quotation = quotation(QuotationStatus.DRAFT);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.updateStatus(OWNER, QUOTATION_ID, QuotationStatus.ACCEPTED)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("QUOTATION_NOT_SENT"));
    }

    @Test
    @DisplayName("updateStatus allows recording acceptance once sent")
    void updateStatusAllowsAcceptedFromSent() {
        Quotation quotation = quotation(QuotationStatus.SENT);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        service.updateStatus(OWNER, QUOTATION_ID, QuotationStatus.ACCEPTED);

        assertThat(quotation.getStatus()).isEqualTo(QuotationStatus.ACCEPTED);
        verify(quotationDao).save(quotation);
    }

    @Test
    @DisplayName("convertToInvoice rejects a quotation that hasn't been accepted")
    void convertToInvoiceRejectsNonAccepted() {
        Quotation quotation = quotation(QuotationStatus.SENT);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.convertToInvoice(OWNER, QUOTATION_ID)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("QUOTATION_NOT_ACCEPTED"));
        verifyNoInteractions(invoiceService);
    }

    @Test
    @DisplayName("convertToInvoice rejects a quotation that was already converted")
    void convertToInvoiceRejectsAlreadyConverted() {
        Quotation quotation = quotation(QuotationStatus.ACCEPTED);
        quotation.setConvertedInvoiceId(99L);
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> service.convertToInvoice(OWNER, QUOTATION_ID)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("ALREADY_CONVERTED"));
        verifyNoInteractions(invoiceService);
    }

    @Test
    @DisplayName("convertToInvoice creates an invoice from an accepted quotation and records the link")
    void convertToInvoiceCreatesInvoiceAndRecordsLink() {
        Quotation quotation = quotation(QuotationStatus.ACCEPTED);
        quotation.setBuyer(BuyerSnapshot.builder().name("Acme Brand").build());
        quotation.addLineItem(QuotationLineItem.builder().description("Sponsored post").quantity(BigDecimal.ONE)
                .rate(new BigDecimal("10000.00")).gstRate(new BigDecimal("18.00")).taxableAmount(new BigDecimal("10000.00")).build());
        when(quotationDao.findByIdAndCreatorId(QUOTATION_ID, OWNER)).thenReturn(Optional.of(quotation));
        when(invoiceService.create(eq(OWNER), any(InvoiceRequest.class))).thenReturn(invoiceDto("55", "INV/2026-27/001"));

        service.convertToInvoice(OWNER, QUOTATION_ID);

        assertThat(quotation.getConvertedInvoiceId()).isEqualTo(55L);
        verify(invoiceService).create(eq(OWNER), any(InvoiceRequest.class));
        verify(quotationDao).save(quotation);
    }

    @Test
    @DisplayName("dispatchDueScheduledSends sends and clears each due quotation, surviving one failure")
    void dispatchDueScheduledSendsProcessesAllAndSurvivesOneFailure() {
        Quotation ok = quotation(QuotationStatus.SENT);
        ok.setId(1L);
        ok.setScheduledSendAt(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));
        ok.setScheduledSendEmail("ok@example.com");

        Quotation failing = quotation(QuotationStatus.DRAFT);
        failing.setId(2L);
        failing.setScheduledSendAt(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));
        failing.setScheduledSendEmail("fail@example.com");

        when(quotationDao.findDueScheduledSends(any())).thenReturn(List.of(ok, failing));
        stubRendering();
        doNothing().when(emailService).sendEmail(eq("ok@example.com"), any(), any(), any(), any());
        doThrow(new IllegalStateException("smtp down")).when(emailService).sendEmail(eq("fail@example.com"), any(), any(), any(), any());

        service.dispatchDueScheduledSends();

        assertThat(ok.getScheduledSendAt()).isNull();
        assertThat(ok.getScheduledSendEmail()).isNull();
        assertThat(ok.getStatus()).isEqualTo(QuotationStatus.SENT);
        verify(quotationDao).save(ok);

        assertThat(failing.getScheduledSendAt()).isNotNull();
        assertThat(failing.getStatus()).isEqualTo(QuotationStatus.DRAFT);
        verify(quotationDao, never()).save(failing);
    }

    private void stubRendering() {
        when(documentRenderer.buildHtml(any(), any())).thenReturn("<html/>");
        when(documentRenderer.buildPlainText(any())).thenReturn("text");
        when(documentRenderer.renderPdf(any())).thenReturn(new byte[] { 1, 2, 3 });
    }

    private static Quotation quotation(QuotationStatus status) {
        return Quotation.builder().id(QUOTATION_ID).status(status).quotationNumber("QUO/2026-27/001").build();
    }

    private static InvoiceDto invoiceDto(String id, String invoiceNumber) {
        return new InvoiceDto(id, invoiceNumber, "2026-27", null, null, InvoiceStatus.DRAFT, false, 0, LocalDate.now(),
                LocalDate.now().plusDays(30), PaymentTerms.NET_30, null, null, null, null, false, false, false, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, TdsSection.NONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, null, null, List.of(), List.of(), null, null, null, null);
    }

    private void authenticateAsOwner(Long creatorId) {
        Creator creator = Creator.builder().id(creatorId).email("owner@example.com").passwordHash("hash").status(CreatorStatus.ACTIVE)
                .role(Role.CREATOR).name("Owner").handle("@owner").build();
        CreatorPrincipal principal = new CreatorPrincipal(creator, creatorId, Set.of(PermissionKey.values()));
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
