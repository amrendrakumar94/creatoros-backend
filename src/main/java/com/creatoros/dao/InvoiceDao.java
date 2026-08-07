package com.creatoros.dao;

import java.util.List;
import java.util.Optional;

import com.creatoros.entity.Invoice;

public interface InvoiceDao {

    Invoice save(Invoice invoice);

    void delete(Invoice invoice);

    List<Invoice> findByCreatorIdOrderByIssueDateDescIdDesc(Long creatorId);

    Optional<Invoice> findByIdAndCreatorId(Long id, Long creatorId);
}
