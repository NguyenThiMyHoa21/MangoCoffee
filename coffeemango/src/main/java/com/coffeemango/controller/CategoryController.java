package com.coffeemango.controller;

import com.coffeemango.model.Category;
import com.coffeemango.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "categories/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Category category,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "categories/form";
        try {
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục")));
        return "categories/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Category category,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "categories/form";
        categoryService.update(id, category);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục thành công!");
        return "redirect:/categories";
    }
}
