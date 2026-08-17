package com.creatoros.daoimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.creatoros.dao.InvoicePaymentDao;
import com.creatoros.entity.InvoicePayment;

@Repository
public class InvoicePaymentDaoImpl extends HibernateDao implements InvoicePaymentDao {

    @Override
    public InvoicePayment save(InvoicePayment payment) {
        return persistOrMerge(payment, payment.getId());
    }

    @Override
    public List<InvoicePayment> findByInvoiceIdOrderByReceivedOnAscIdAsc(Long invoiceId) {
        return session()
                .createSelectionQuery("from InvoicePayment p where p.invoice.id = :invoiceId order by p.receivedOn asc, p.id asc",
                        InvoicePayment.class)
                .setParameter("invoiceId", invoiceId).getResultList();
    }

    @Override
    public List<InvoicePayment> findByCreatorIdOrderByReceivedOnAscIdAsc(Long creatorId) {
        return session()
                .createSelectionQuery("from InvoicePayment p where p.creator.id = :creatorId order by p.receivedOn asc, p.id asc",
                        InvoicePayment.class)
                .setParameter("creatorId", creatorId).getResultList();
    }

    @Override
    public Optional<InvoicePayment> findByRazorpayPaymentId(String razorpayPaymentId) {
        return session().createSelectionQuery("from InvoicePayment p where p.razorpayPaymentId = :razorpayPaymentId", InvoicePayment.class)
                .setParameter("razorpayPaymentId", razorpayPaymentId).uniqueResultOptional();
    }
}
