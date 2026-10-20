package com.coffeemango.controller;

import com.coffeemango.model.Product;
import com.coffeemango.service.CategoryService;
import com.coffeemango.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("products", productService.search(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("products", productService.findAll());
        }
        return "products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAllActive());
        return "products/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Product product,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActive());
            return "products/form";
        }
        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAllActive());
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Product product,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActive());
            return "products/form";
        }
        productService.update(id, product);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn sản phẩm thành công!");
        return "redirect:/products";
    }
}
