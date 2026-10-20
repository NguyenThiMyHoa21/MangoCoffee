package com.coffeemango.controller;

import com.coffeemango.service.GeminiAIService;
import com.coffeemango.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Controller
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final InvoiceService invoiceService;
    private final GeminiAIService geminiAIService;

    @GetMapping
    public String revenueStats(Model model,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime start = (from != null) ? from.atStartOfDay()
                : LocalDateTime.now().minusDays(6).with(LocalTime.MIN);
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();

        model.addAttribute("totalRevenue", invoiceService.getRevenue(start, end));
        model.addAttribute("topProducts", invoiceService.getTopSellingProducts(start, end));
        model.addAttribute("dailyRevenue", invoiceService.getDailyRevenue(start, end));
        model.addAttribute("from", from != null ? from : LocalDate.now().minusDays(6));
        model.addAttribute("to", to != null ? to : LocalDate.now());

        return "revenue/stats";
    }

    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<Map<String, String>> analyzeRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime start = (from != null) ? from.atStartOfDay()
                : LocalDateTime.now().minusDays(6).with(LocalTime.MIN);
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();

        String analysis = geminiAIService.analyzeRevenue(start, end);
        return ResponseEntity.ok(Map.of("analysis", analysis));
    }
}
