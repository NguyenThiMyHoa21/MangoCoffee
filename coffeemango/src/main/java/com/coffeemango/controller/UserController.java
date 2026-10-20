package com.coffeemango.controller;

import com.coffeemango.model.User;
import com.coffeemango.model.Role;
import com.coffeemango.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("staffList", userService.findAll());
        return "staff/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "staff/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute User user,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "staff/form";
        }
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Tên đăng nhập đã tồn tại");
            model.addAttribute("roles", Role.values());
            return "staff/form";
        }
        userService.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên thành công!");
        return "redirect:/staff";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên")));
        model.addAttribute("roles", Role.values());
        return "staff/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute User user,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "staff/form";
        }
        userService.update(id, user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công!");
        return "redirect:/staff";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nhân viên thành công khỏi hệ thống!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff";
    }
}
