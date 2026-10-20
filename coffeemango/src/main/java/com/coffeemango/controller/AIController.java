package com.coffeemango.controller;

import com.coffeemango.model.User;
import com.coffeemango.service.GeminiAIService;
import com.coffeemango.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final GeminiAIService geminiAIService;
    private final UserService userService;

    @GetMapping
    public String aiPage(Model model, Authentication authentication) {
        if (authentication != null) {
            userService.findByUsername(authentication.getName())
                    .ifPresent(user -> model.addAttribute("currentUser", user));
        }
        return "ai/assistant";
    }

    @PostMapping("/ask")
    @ResponseBody
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> body,
                                                  Authentication authentication) {
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("answer", "Vui lòng nhập câu hỏi."));
        }

        User currentUser = null;
        if (authentication != null) {
            currentUser = userService.findByUsername(authentication.getName()).orElse(null);
        }

        String answer = geminiAIService.askQuestion(question, currentUser);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
