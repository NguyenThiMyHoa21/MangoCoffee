package com.coffeemango.controller;

import com.coffeemango.model.CoffeeTable;
import com.coffeemango.service.CoffeeTableService;
import com.coffeemango.service.InvoiceService;
import com.coffeemango.service.ProductService;
import com.coffeemango.service.UserService;
import com.coffeemango.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final InvoiceService invoiceService;
    private final ProductService productService;
    private final UserService userService;
    private final IngredientService ingredientService;
    private final CoffeeTableService tableService;

    @GetMapping
    public String dashboard(Model model) {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);
        LocalDateTime weekStart = LocalDateTime.now().minusDays(6).with(LocalTime.MIN);

        model.addAttribute("todayRevenue", invoiceService.getRevenue(todayStart, todayEnd));
        model.addAttribute("weekRevenue", invoiceService.getRevenue(weekStart, todayEnd));
        model.addAttribute("totalProducts", productService.findAllAvailable().size());
        // Chỉ đếm những nhân sự đang hoạt động trong ca trực
        model.addAttribute("totalStaff", userService.findActiveStaff().size());
        model.addAttribute("lowStockIngredients", ingredientService.findLowStock());
        model.addAttribute("topProducts", invoiceService.getTopSellingProducts(weekStart, todayEnd));
        model.addAttribute("recentInvoices", invoiceService.findByDateRange(todayStart, todayEnd));

        // Dữ liệu vận hành bàn cho nhân viên
        model.addAttribute("occupiedTables", tableService.countByStatus(CoffeeTable.TableStatus.OCCUPIED));
        model.addAttribute("availableTables", tableService.countByStatus(CoffeeTable.TableStatus.AVAILABLE));
        model.addAttribute("reservedTables", tableService.countByStatus(CoffeeTable.TableStatus.RESERVED));

        return "dashboard/index";
    }
}
