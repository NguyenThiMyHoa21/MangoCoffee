package com.coffeemango.controller;

import com.coffeemango.model.CoffeeTable;
import com.coffeemango.model.CoffeeTable.TableStatus;
import com.coffeemango.model.Invoice;
import com.coffeemango.service.CoffeeTableService;
import com.coffeemango.service.InvoiceService;
import com.coffeemango.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableController {

    private final CoffeeTableService tableService;
    private final InvoiceService invoiceService;
    private final ProductService productService;
    private final com.coffeemango.service.CategoryService categoryService;

    // Xem sơ đồ bàn trực quan (map.html)
    @GetMapping
    public String viewTables(Model model) {
        List<CoffeeTable> tables = tableService.findAll();
        model.addAttribute("tables", tables);

        // Map hóa đơn đang phục vụ theo bàn
        Map<Long, Invoice> tableInvoiceMap = new HashMap<>();
        for (Invoice inv : invoiceService.findActiveInvoices()) {
            if (inv.getCoffeeTable() != null) {
                tableInvoiceMap.put(inv.getCoffeeTable().getId(), inv);
            }
        }
        model.addAttribute("tableInvoiceMap", tableInvoiceMap);
        model.addAttribute("statusCount", tableService.getTableStatusCount());
        return "tables/map";
    }

    // Đặt món tại bàn
    @GetMapping("/{id}/order")
    public String orderTable(@PathVariable Long id, Model model) {
        CoffeeTable table = tableService.findById(id);
        if (table == null) {
            return "redirect:/tables";
        }
        model.addAttribute("table", table);
        model.addAttribute("products", productService.findAllAvailable());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("activeInvoice", invoiceService.findActiveInvoiceByTable(id).orElse(null));
        return "tables/order";
    }

    // Trang quản lý bàn (Dành cho Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/manage")
    public String manageTables(Model model) {
        model.addAttribute("tables", tableService.findAll());
        return "tables/manage";
    }

    // Thêm bàn mới
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public String createTable(@ModelAttribute CoffeeTable table, RedirectAttributes ra) {
        tableService.save(table);
        ra.addFlashAttribute("successMessage", "Thêm bàn mới thành công!");
        return "redirect:/tables/manage";
    }

    // Xóa bàn
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/delete/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String deleteTable(@PathVariable Long id, RedirectAttributes ra) {
        try {
            tableService.delete(id);
            ra.addFlashAttribute("successMessage", "Xóa bàn thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa bàn vì đang có dữ liệu hóa đơn liên kết!");
        }
        return "redirect:/tables/manage";
    }

    // API: Đổi nhanh trạng thái bàn từ map.html
    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<?> setStatusQuick(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            TableStatus status = TableStatus.valueOf(body.get("status"));
            tableService.setStatus(id, status);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // API: Cập nhật trạng thái bàn (tương thích form cũ)
    @PostMapping("/api/update-status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@RequestParam Long tableId, @RequestParam String status) {
        try {
            TableStatus tableStatus = TableStatus.valueOf(status);
            tableService.setStatus(tableId, tableStatus);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // API: Lấy danh sách bàn trống
    @GetMapping("/api/available")
    @ResponseBody
    public ResponseEntity<?> getAvailableTables() {
        return ResponseEntity.ok(tableService.getAvailableTables());
    }
}
