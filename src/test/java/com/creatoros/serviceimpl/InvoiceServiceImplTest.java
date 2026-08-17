package com.creatoros.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import com.creatoros.dao.InvoiceDao;
import com.creatoros.dao.InvoicePaymentDao;
import com.creatoros.entity.BuyerSnapshot;
import com.creatoros.entity.Creator;
import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoicePayment;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.InvoiceStatus;
import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;
import com.creatoros.exception.BadRequestException;
import com.creatoros.security.CreatorPrincipal;
import com.creatoros.service.DocumentNumberService;
import com.creatoros.service.GstCalculationService;
import com.creatoros.util.EmailService;
import com.creatoros.util.InvoiceDocumentRenderer;
import com.creatoros.util.RazorpayService;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    private static final Long OWNER      = 1L;
    private static final Long INVOICE_ID = 10L;

    @Mock
    private InvoiceDao invoiceDao;

    @Mock
    private InvoicePaymentDao invoicePaymentDao;

    @Mock
    private CreatorDao creatorDao;

    @Mock
    private BrandDealDao brandDealDao;

    @Mock
    private DocumentNumberService documentNumberService;

    @Mock
    private GstCalculationService gstCalculationService;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private InvoiceDocumentRenderer documentRenderer;

    @Mock
    private RazorpayService razorpayService;

    private InvoiceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InvoiceServiceImpl(invoiceDao, invoicePaymentDao, creatorDao, brandDealDao, documentNumberService, gstCalculationService,
                invoiceMapper, emailService, documentRenderer, razorpayService);
        authenticateAsOwner(OWNER);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("sendNow dispatches an email and flips a draft invoice to Sent")
    void sendNowDispatchesEmailAndFlipsDraftToSent() {
        Invoice invoice = invoice(InvoiceStatus.DRAFT);
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));
        stubRendering();

        service.sendNow(OWNER, INVOICE_ID, "brand@example.com");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getLastEmailedAt()).isNotNull();
        verify(emailService).sendEmail(eq("brand@example.com"), any(), any(), any(), any());
        verify(invoiceDao).save(invoice);
    }

    @Test
    @DisplayName("sendNow leaves an already-issued invoice's status unchanged")
    void sendNowLeavesIssuedStatusUnchanged() {
        Invoice invoice = invoice(InvoiceStatus.PAID);
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));
        stubRendering();

        service.sendNow(OWNER, INVOICE_ID, "brand@example.com");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getLastEmailedAt()).isNotNull();
    }

    @Test
    @DisplayName("sendNow rejects a cancelled invoice")
    void sendNowRejectsCancelledInvoice() {
        Invoice invoice = invoice(InvoiceStatus.CANCELLED);
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service.sendNow(OWNER, INVOICE_ID, "brand@example.com")).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("INVOICE_CANCELLED"));
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("scheduleSend stores the recipient and time for a future send")
    void scheduleSendStoresFutureSend() {
        Invoice invoice = invoice(InvoiceStatus.SENT);
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        service.scheduleSend(OWNER, INVOICE_ID, "brand@example.com", future);

        assertThat(invoice.getScheduledSendEmail()).isEqualTo("brand@example.com");
        assertThat(invoice.getScheduledSendAt()).isEqualTo(Timestamp.valueOf(future));
    }

    @Test
    @DisplayName("scheduleSend rejects a time in the past")
    void scheduleSendRejectsPastTime() {
        Invoice invoice = invoice(InvoiceStatus.SENT);
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service.scheduleSend(OWNER, INVOICE_ID, "brand@example.com", LocalDateTime.now().minusHours(1)))
                .isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("SCHEDULE_TIME_IN_PAST"));
    }

    @Test
    @DisplayName("cancelScheduledSend clears a pending schedule")
    void cancelScheduledSendClearsSchedule() {
        Invoice invoice = invoice(InvoiceStatus.SENT);
        invoice.setScheduledSendAt(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        invoice.setScheduledSendEmail("brand@example.com");
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));

        service.cancelScheduledSend(OWNER, INVOICE_ID);

        assertThat(invoice.getScheduledSendAt()).isNull();
        assertThat(invoice.getScheduledSendEmail()).isNull();
    }

    @Test
    @DisplayName("requireEditable allows a paid invoice to be edited")
    void requireEditableAllowsPaidInvoice() {
        Invoice invoice = invoice(InvoiceStatus.PAID);
        invoice.setAmountPaid(new BigDecimal("500.00"));

        assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "requireEditable", invoice)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireEditable still blocks a cancelled invoice")
    void requireEditableBlocksCancelledInvoice() {
        Invoice invoice = invoice(InvoiceStatus.CANCELLED);

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "requireEditable", invoice)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("INVOICE_CANCELLED"));
    }

    @Test
    @DisplayName("createPaymentLink rejects a draft invoice")
    void createPaymentLinkRejectsADraftInvoice() {
        Invoice invoice = invoice(InvoiceStatus.DRAFT);
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service.createPaymentLink(OWNER, INVOICE_ID)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("INVOICE_NOT_PAYABLE"));
        verifyNoInteractions(razorpayService);
    }

    @Test
    @DisplayName("createPaymentLink rejects an invoice with nothing outstanding")
    void createPaymentLinkRejectsWhenNothingOutstanding() {
        Invoice invoice = payableInvoice(BigDecimal.ZERO);
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service.createPaymentLink(OWNER, INVOICE_ID)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("INVOICE_NOT_PAYABLE"));
        verifyNoInteractions(razorpayService);
    }

    @Test
    @DisplayName("createPaymentLink creates a link for the balance due and stores it on the invoice")
    void createPaymentLinkCreatesAndStoresALink() {
        Invoice invoice = payableInvoice(new BigDecimal("500.00"));
        when(invoiceDao.findByIdAndCreatorId(INVOICE_ID, OWNER)).thenReturn(Optional.of(invoice));
        when(razorpayService.createPaymentLink(eq(new BigDecimal("500.00")), any(), eq("invoice:" + INVOICE_ID), eq("Acme Brand"),
                eq("brand@example.com"), isNull())).thenReturn(new RazorpayService.PaymentLink("plink_test", "https://rzp.io/test"));

        service.createPaymentLink(OWNER, INVOICE_ID);

        assertThat(invoice.getRazorpayPaymentLinkId()).isEqualTo("plink_test");
        assertThat(invoice.getRazorpayPaymentLinkUrl()).isEqualTo("https://rzp.io/test");
        verify(invoiceDao).save(invoice);
    }

    @Test
    @DisplayName("recordGatewayPayment ignores a re-delivered webhook for the same payment")
    void recordGatewayPaymentIsIdempotent() {
        when(invoicePaymentDao.findByRazorpayPaymentId("pay_123"))
                .thenReturn(Optional.of(InvoicePayment.builder().razorpayPaymentId("pay_123").build()));

        service.recordGatewayPayment(INVOICE_ID, new BigDecimal("500.00"), "pay_123");

        verify(invoiceDao, never()).findById(any());
        verify(invoicePaymentDao, never()).save(any());
        verify(invoiceDao, never()).save(any());
    }

    @Test
    @DisplayName("recordGatewayPayment records the payment and settles the invoice")
    void recordGatewayPaymentRecordsPaymentAndSettlesInvoice() {
        Invoice invoice = payableInvoice(new BigDecimal("1000.00"));
        invoice.setInvoiceTotal(new BigDecimal("1000.00"));
        when(invoicePaymentDao.findByRazorpayPaymentId("pay_123")).thenReturn(Optional.empty());
        when(invoiceDao.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoicePaymentDao.findByInvoiceIdOrderByReceivedOnAscIdAsc(INVOICE_ID))
                .thenReturn(List.of(InvoicePayment.builder().amount(new BigDecimal("500.00")).tdsWithheld(BigDecimal.ZERO).build()));

        service.recordGatewayPayment(INVOICE_ID, new BigDecimal("500.00"), "pay_123");

        assertThat(invoice.getAmountPaid()).isEqualTo(new BigDecimal("500.00"));
        assertThat(invoice.getBalanceDue()).isEqualTo(new BigDecimal("500.00"));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        verify(invoicePaymentDao).save(any(InvoicePayment.class));
        verify(invoiceDao).save(invoice);
    }

    @Test
    @DisplayName("dispatchDueScheduledSends sends and clears each due invoice, surviving one failure")
    void dispatchDueScheduledSendsProcessesAllAndSurvivesOneFailure() {
        Invoice ok = invoice(InvoiceStatus.SENT);
        ok.setId(1L);
        ok.setScheduledSendAt(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));
        ok.setScheduledSendEmail("ok@example.com");

        Invoice failing = invoice(InvoiceStatus.DRAFT);
        failing.setId(2L);
        failing.setScheduledSendAt(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));
        failing.setScheduledSendEmail("fail@example.com");

        when(invoiceDao.findDueScheduledSends(any())).thenReturn(List.of(ok, failing));
        stubRendering();
        doNothing().when(emailService).sendEmail(eq("ok@example.com"), any(), any(), any(), any());
        doThrow(new IllegalStateException("smtp down")).when(emailService).sendEmail(eq("fail@example.com"), any(), any(), any(), any());

        service.dispatchDueScheduledSends();

        assertThat(ok.getScheduledSendAt()).isNull();
        assertThat(ok.getScheduledSendEmail()).isNull();
        assertThat(ok.getStatus()).isEqualTo(InvoiceStatus.SENT);
        verify(invoiceDao).save(ok);

        // The failing invoice's email throws before the status/schedule fields are cleared, so it's
        // left untouched to be retried on the next poll rather than silently marked as sent.
        assertThat(failing.getScheduledSendAt()).isNotNull();
        assertThat(failing.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        verify(invoiceDao, never()).save(failing);
    }

    private void stubRendering() {
        when(documentRenderer.buildHtml(any(), any())).thenReturn("<html/>");
        when(documentRenderer.buildPlainText(any())).thenReturn("text");
        when(documentRenderer.renderPdf(any())).thenReturn(new byte[] { 1, 2, 3 });
    }

    private static Invoice invoice(InvoiceStatus status) {
        return Invoice.builder().id(INVOICE_ID).status(status).invoiceNumber("INV-2026-0001").amountPaid(BigDecimal.ZERO)
                .invoiceTotal(BigDecimal.ZERO).balanceDue(BigDecimal.ZERO).build();
    }

    private static Invoice payableInvoice(BigDecimal balanceDue) {
        return Invoice.builder().id(INVOICE_ID).status(InvoiceStatus.SENT).invoiceNumber("INV-2026-0001").amountPaid(BigDecimal.ZERO)
                .invoiceTotal(BigDecimal.ZERO).balanceDue(balanceDue)
                .buyer(BuyerSnapshot.builder().name("Acme Brand").email("brand@example.com").build()).build();
    }

    private void authenticateAsOwner(Long creatorId) {
        Creator creator = Creator.builder().id(creatorId).email("owner@example.com").passwordHash("hash").status(CreatorStatus.ACTIVE)
                .role(Role.CREATOR).name("Owner").handle("@owner").build();
        CreatorPrincipal principal = new CreatorPrincipal(creator, creatorId, Set.of(PermissionKey.values()));
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
