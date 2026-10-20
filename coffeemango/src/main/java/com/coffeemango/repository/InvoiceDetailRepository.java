package com.coffeemango.repository;

import com.coffeemango.model.InvoiceDetail;
import com.coffeemango.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {
    List<InvoiceDetail> findByInvoice(Invoice invoice);
}
