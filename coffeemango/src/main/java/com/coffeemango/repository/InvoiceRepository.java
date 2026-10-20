package com.coffeemango.repository;

import com.coffeemango.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    Optional<Invoice> findFirstByCoffeeTableIdAndStatusOrderByCreatedAtDesc(Long tableId, Invoice.InvoiceStatus status);

    List<Invoice> findByStatus(Invoice.InvoiceStatus status);

    List<Invoice> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(i.finalAmount), 0) FROM Invoice i " +
           "WHERE i.status = 'COMPLETED' AND i.createdAt BETWEEN :start AND :end")
    BigDecimal sumRevenueByDateRange(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // Top sản phẩm bán chạy trong khoảng thời gian
    @Query("SELECT d.product.name, SUM(d.quantity) as totalQty, SUM(d.subtotal) as totalRevenue " +
           "FROM InvoiceDetail d JOIN d.invoice i " +
           "WHERE i.status = 'COMPLETED' AND i.createdAt BETWEEN :start AND :end " +
           "GROUP BY d.product.name ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    // Doanh thu theo ngày
    @Query("SELECT DATE(i.createdAt), SUM(i.finalAmount) FROM Invoice i " +
           "WHERE i.status = 'COMPLETED' AND i.createdAt BETWEEN :start AND :end " +
           "GROUP BY DATE(i.createdAt) ORDER BY DATE(i.createdAt)")
    List<Object[]> findDailyRevenue(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Modifying
    @Query("UPDATE Invoice i SET i.staff = null WHERE i.staff.id = :staffId")
    void unlinkStaffFromInvoices(@Param("staffId") Long staffId);
}
