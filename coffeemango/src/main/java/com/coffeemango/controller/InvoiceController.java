package com.coffeemango.controller;

import com.coffeemango.model.CoffeeTable;
import com.coffeemango.model.Invoice;
import com.coffeemango.model.InvoiceDetail;
import com.coffeemango.model.Product;
import com.coffeemango.service.CoffeeTableService;
import com.coffeemango.service.InvoiceService;
import com.coffeemango.service.ProductService;
import com.coffeemango.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ProductService productService;
    private final UserService userService;
    private final CoffeeTableService tableService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDateTime.now().minusDays(30);
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();
        model.addAttribute("invoices", invoiceService.findByDateRange(start, end));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "invoices/list";
    }

    @GetMapping("/create")
    public String createForm(Model model, @RequestParam(required = false) Long tableId) {
        model.addAttribute("products", productService.findAllAvailable());
        model.addAttribute("tables", tableService.findAll());
        model.addAttribute("selectedTableId", tableId);
        return "invoices/form";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        model.addAttribute("invoice", invoice);
        return "invoices/detail";
    }

    @GetMapping("/print/{id}")
    public String print(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        model.addAttribute("invoice", invoice);
        return "invoices/print";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createInvoiceApi(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        try {
            boolean payNow = !body.containsKey("payNow") || Boolean.parseBoolean(body.get("payNow").toString());
            Long existingInvoiceId = (body.containsKey("invoiceId") && body.get("invoiceId") != null && !body.get("invoiceId").toString().isBlank())
                    ? Long.parseLong(body.get("invoiceId").toString()) : null;

            // Parse chi tiết món
            List<Map<String, Object>> detailsData =
                    (List<Map<String, Object>>) body.get("details");
            List<InvoiceDetail> details = new ArrayList<>();
            for (Map<String, Object> d : detailsData) {
                InvoiceDetail detail = new InvoiceDetail();
                Map<String, Object> productMap = (Map<String, Object>) d.get("product");
                Long productId = Long.parseLong(productMap.get("id").toString());
                Product product = productService.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + productId));
                detail.setProduct(product);
                detail.setQuantity(Integer.parseInt(d.get("quantity").toString()));
                detail.setUnitPrice(new BigDecimal(d.get("unitPrice").toString()));
                detail.setSubtotal(new BigDecimal(d.get("subtotal").toString()));
                details.add(detail);
            }

            Invoice saved;
            if (existingInvoiceId != null) {
                // Cập nhật hóa đơn đang chờ của bàn
                Invoice updateData = new Invoice();
                updateData.setPaymentMethod((String) body.get("paymentMethod"));
                updateData.setNote((String) body.get("note"));
                updateData.setDiscount(new BigDecimal(body.get("discount").toString()));
                updateData.setDetails(details);
                saved = invoiceService.updateInvoice(existingInvoiceId, updateData);
            } else {
                // Tạo hóa đơn mới
                Invoice invoice = new Invoice();
                if (authentication != null) {
                    userService.findByUsername(authentication.getName())
                            .ifPresent(invoice::setStaff);
                }
                if (body.containsKey("tableId") && body.get("tableId") != null && !body.get("tableId").toString().isBlank()) {
                    Long tableId = Long.parseLong(body.get("tableId").toString());
                    CoffeeTable table = tableService.findById(tableId);
                    if (table != null) {
                        invoice.setCoffeeTable(table);
                    }
                }
                invoice.setPaymentMethod((String) body.get("paymentMethod"));
                invoice.setNote((String) body.get("note"));
                invoice.setDiscount(new BigDecimal(body.get("discount").toString()));
                invoice.setDetails(details);
                saved = invoiceService.createInvoice(invoice);
            }

            // Xử lý trạng thái thanh toán và trạng thái bàn
            if (payNow) {
                invoiceService.completeInvoice(saved.getId());
                if (saved.getCoffeeTable() != null) {
                    tableService.setStatus(saved.getCoffeeTable().getId(), CoffeeTable.TableStatus.AVAILABLE);
                }
            } else {
                // Khách ngồi uống -> giữ bàn OCCUPIED
                if (saved.getCoffeeTable() != null) {
                    tableService.setStatus(saved.getCoffeeTable().getId(), CoffeeTable.TableStatus.OCCUPIED);
                }
            }

            response.put("success", true);
            response.put("invoiceId", saved.getId());
            response.put("payNow", payNow);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/complete/{id}")
    public String complete(@PathVariable Long id, RedirectAttributes ra) {
        invoiceService.completeInvoice(id);
        ra.addFlashAttribute("successMessage", "Hóa đơn đã được thanh toán!");
        return "redirect:/invoices";
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        invoiceService.cancelInvoice(id);
        ra.addFlashAttribute("successMessage", "Đã hủy hóa đơn!");
        return "redirect:/invoices";
    }
}
