package com.coffeemango.service;

import com.coffeemango.model.Invoice;
import com.coffeemango.model.InvoiceDetail;
import com.coffeemango.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    public List<Invoice> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return invoiceRepository.findByCreatedAtBetween(start, end);
    }

    // =========================================================================================
    // [VẤN ĐÁP]: QUY TRÌNH TẠO HÓA ĐƠN POS, TÍNH TIỀN VÀ TRỪ GIẢM GIÁ
    // -----------------------------------------------------------------------------------------
    // 1. Tự sinh mã hóa đơn duy nhất: generateInvoiceCode() -> Dạng INV-xxxxxx
    // 2. Tính tổng tiền: Dùng Java Stream duyệt qua từng món (đơn giá * số lượng)
    // 3. Tính tiền thực thu: FinalAmount = TotalAmount - Discount (trừ chiết khấu giảm giá)
    // 4. Lưu InvoiceDetail và lưu Invoice vào MySQL qua invoiceRepository.save()
    // =========================================================================================
    public Invoice createInvoice(Invoice invoice) {
        invoice.setInvoiceCode(generateInvoiceCode());
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);

        // Tính tổng tiền
        BigDecimal total = invoice.getDetails().stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setTotalAmount(total);

        BigDecimal discount = invoice.getDiscount() != null ? invoice.getDiscount() : BigDecimal.ZERO;
        invoice.setFinalAmount(total.subtract(discount));

        // Gán invoice vào các detail
        for (InvoiceDetail detail : invoice.getDetails()) {
            detail.setInvoice(invoice);
            detail.setSubtotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
        }

        return invoiceRepository.save(invoice);
    }

    public Optional<Invoice> findActiveInvoiceByTable(Long tableId) {
        return invoiceRepository.findFirstByCoffeeTableIdAndStatusOrderByCreatedAtDesc(tableId, Invoice.InvoiceStatus.PENDING);
    }

    public List<Invoice> findActiveInvoices() {
        return invoiceRepository.findByStatus(Invoice.InvoiceStatus.PENDING);
    }

    public Invoice updateInvoice(Long id, Invoice updatedData) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể chỉnh sửa hóa đơn đang chờ thanh toán!");
        }

        invoice.getDetails().clear();
        if (updatedData.getDetails() != null) {
            for (InvoiceDetail detail : updatedData.getDetails()) {
                detail.setInvoice(invoice);
                detail.setSubtotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                invoice.getDetails().add(detail);
            }
        }

        BigDecimal total = invoice.getDetails().stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setTotalAmount(total);

        BigDecimal discount = updatedData.getDiscount() != null ? updatedData.getDiscount() : BigDecimal.ZERO;
        invoice.setDiscount(discount);
        invoice.setFinalAmount(total.subtract(discount));

        if (updatedData.getPaymentMethod() != null) {
            invoice.setPaymentMethod(updatedData.getPaymentMethod());
        }
        if (updatedData.getNote() != null) {
            invoice.setNote(updatedData.getNote());
        }

        return invoiceRepository.save(invoice);
    }

    public Invoice completeInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        invoice.setStatus(Invoice.InvoiceStatus.COMPLETED);
        return invoiceRepository.save(invoice);
    }

    public Invoice cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
        return invoiceRepository.save(invoice);
    }

    public BigDecimal getRevenue(LocalDateTime start, LocalDateTime end) {
        return invoiceRepository.sumRevenueByDateRange(start, end);
    }

    public List<Object[]> getTopSellingProducts(LocalDateTime start, LocalDateTime end) {
        return invoiceRepository.findTopSellingProducts(start, end);
    }

    public List<Object[]> getDailyRevenue(LocalDateTime start, LocalDateTime end) {
        return invoiceRepository.findDailyRevenue(start, end);
    }

    private String generateInvoiceCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        int random = new Random().nextInt(9000) + 1000;
        return "INV-" + timestamp + "-" + random;
    }
}
