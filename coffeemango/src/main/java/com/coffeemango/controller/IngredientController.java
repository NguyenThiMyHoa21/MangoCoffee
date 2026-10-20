package com.coffeemango.controller;

import com.coffeemango.model.Ingredient;
import com.coffeemango.service.GeminiAIService;
import com.coffeemango.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final GeminiAIService geminiAIService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("ingredients", ingredientService.findAll());
        model.addAttribute("lowStockIngredients", ingredientService.findLowStock());
        return "ingredients/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("ingredient", new Ingredient());
        return "ingredients/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Ingredient ingredient,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "ingredients/form";
        try {
            ingredientService.save(ingredient);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nguyên liệu thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ingredients";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("ingredient", ingredientService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu")));
        return "ingredients/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Ingredient ingredient,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "ingredients/form";
        ingredientService.update(id, ingredient);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nguyên liệu thành công!");
        return "redirect:/ingredients";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ingredientService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa nguyên liệu thành công!");
        return "redirect:/ingredients";
    }

    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<Map<String, String>> analyzeInventory() {
        String analysis = geminiAIService.analyzeInventory();
        return ResponseEntity.ok(Map.of("analysis", analysis));
    }
}
