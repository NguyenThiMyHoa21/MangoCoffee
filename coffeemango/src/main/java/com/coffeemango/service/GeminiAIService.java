package com.coffeemango.service;

import com.coffeemango.model.Ingredient;
import com.coffeemango.model.Role;
import com.coffeemango.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;
    private final InvoiceService invoiceService;
    private final IngredientService ingredientService;

    /**
     * Hỏi đáp tự do - AI Assistant (tương thích ngược)
     */
    public String askQuestion(String question) {
        return askQuestion(question, null);
    }

    /**
     * Hỏi đáp tự do - AI Assistant nhận diện danh tính và vai trò người dùng
     */
    public String askQuestion(String question, User currentUser) {
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY") || apiKey.isBlank() || apiKey.length() < 10) {
            return "⚠️ Chưa cấu hình Gemini API Key. Vui lòng điền key vào application.properties.";
        }

        StringBuilder context = new StringBuilder();
        context.append("Bạn là trợ lý AI thông minh của quán cà phê Coffee Mango.\n");
        context.append("Hãy trả lời bằng tiếng Việt tự nhiên, thân thiện, ngắn gọn, súc tích và có emoji phù hợp.\n\n");

        boolean isStaff = currentUser != null && currentUser.getRole() == Role.STAFF;

        // Phân quyền ngữ cảnh và định hướng xưng hô theo người dùng
        if (currentUser != null) {
            String roleDesc = switch (currentUser.getRole()) {
                case ADMIN -> "Chủ quán / Quản trị viên (ADMIN)";
                case MANAGER -> "Quản lý cửa hàng (MANAGER)";
                case STAFF -> "Nhân viên phục vụ (STAFF)";
            };

            context.append("=== THÔNG TIN NGƯỜI ĐANG NÓI CHUYỆN VỚI BẠN ===\n");
            context.append("- Họ và tên: ").append(currentUser.getFullName()).append("\n");
            context.append("- Tên đăng nhập: ").append(currentUser.getUsername()).append("\n");
            context.append("- Vai trò: ").append(roleDesc).append("\n\n");

            context.append("=== HƯỚNG DẪN XƯNG HÔ VÀ TƯ VẤN CHO VAI TRÒ NÀY ===\n");
            if (currentUser.getRole() == Role.ADMIN) {
                context.append("- Hãy xưng hô lịch sự, tôn trọng (ví dụ: 'Dạ em chào Admin ").append(currentUser.getFullName())
                       .append(" ạ' hoặc 'Dạ chào anh/chị ").append(currentUser.getFullName()).append("').\n");
                context.append("- Trọng tâm tư vấn: Chiến lược kinh doanh, tối ưu chi phí, doanh thu - lợi nhuận, hiệu quả vận hành và mở rộng.\n\n");
            } else if (currentUser.getRole() == Role.MANAGER) {
                context.append("- Hãy xưng hô thân thiện, phối hợp tốt (ví dụ: 'Dạ chào Quản lý ").append(currentUser.getFullName()).append(" ạ').\n");
                context.append("- Trọng tâm tư vấn: Vận hành ca làm việc, kiểm soát tồn kho nguyên liệu, chăm sóc khách hàng, đẩy doanh số bán hàng trong tuần.\n\n");
            } else {
                context.append("- Hãy xưng hô gần gũi, đồng hành (ví dụ: 'Chào bạn ").append(currentUser.getFullName()).append("').\n");
                context.append("- Trọng tâm tư vấn: Hỗ trợ quy trình order, phục vụ khách, pha chế, lưu ý ca làm. Tuyệt đối KHÔNG tiết lộ số liệu tài chính hoặc doanh thu tổng cho nhân viên.\n\n");
            }
        } else {
            context.append("=== HƯỚNG DẪN XƯNG HÔ ===\n");
            context.append("- Người hỏi là Quản lý quán. Hãy xưng hô thân thiện, lịch sự.\n\n");
        }

        // Thu thập dữ liệu thực tế từ DB
        LocalDateTime weekStart = LocalDateTime.now().minusDays(6).with(java.time.LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();

        context.append("=== DỮ LIỆU THỰC TẾ HIỆN TẠI CỦA QUÁN ===\n");
        if (!isStaff) {
            BigDecimal weekRevenue = invoiceService.getRevenue(weekStart, now);
            context.append("Doanh thu 7 ngày gần nhất: ").append(String.format("%,.0f", weekRevenue)).append(" VNĐ\n");
        }

        List<Object[]> topProducts = invoiceService.getTopSellingProducts(weekStart, now);
        if (!topProducts.isEmpty()) {
            context.append("Top sản phẩm bán chạy tuần này:\n");
            for (int i = 0; i < Math.min(5, topProducts.size()); i++) {
                Object[] row = topProducts.get(i);
                context.append("  ").append(i + 1).append(". ").append(row[0])
                       .append(" (").append(row[1]).append(" ly)\n");
            }
        }

        context.append("\n=== CÂU HỎI CỦA NGƯỜI DÙNG ===\n");
        context.append(question);
        context.append("\n\nHãy trả lời dựa trên dữ liệu thực tế ở trên (nếu liên quan), kết hợp kiến thức chuyên môn và phù hợp với vai trò của người hỏi.");

        return callGeminiAPI(context.toString());
    }

    public String analyzeRevenue(LocalDateTime start, LocalDateTime end) {
        // Kiểm tra API key
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY") || apiKey.isBlank() || apiKey.length() < 10) {
            return "⚠️ Chưa cấu hình Gemini API Key.\n\nVui lòng:\n1. Vào https://aistudio.google.com/apikey\n2. Tạo API Key miễn phí\n3. Điền vào application.properties: gemini.api.key=YOUR_KEY\n4. Restart ứng dụng";
        }

        // Thu thập dữ liệu từ database
        BigDecimal currentRevenue = invoiceService.getRevenue(start, end);
        List<Object[]> topProducts = invoiceService.getTopSellingProducts(start, end);
        List<Object[]> dailyRevenue = invoiceService.getDailyRevenue(start, end);

        LocalDateTime prevStart = start.minusDays(7);
        LocalDateTime prevEnd = end.minusDays(7);
        BigDecimal previousRevenue = invoiceService.getRevenue(prevStart, prevEnd);

        String prompt = buildAnalysisPrompt(currentRevenue, previousRevenue, topProducts, dailyRevenue, start, end);
        return callGeminiAPI(prompt);
    }

    private String buildAnalysisPrompt(BigDecimal current, BigDecimal previous,
                                        List<Object[]> topProducts,
                                        List<Object[]> dailyRevenue,
                                        LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();

        sb.append("Bạn là chuyên gia phân tích kinh doanh cho quán cà phê Coffee Mango.\n");
        sb.append("QUY TẮC BẮT BUỘC: Hãy trả lời 100% bằng TIẾNG VIỆT, ngắn gọn, súc tích, chuyên nghiệp. Tuyệt đối KHÔNG dùng tiếng Anh.\n\n");
        sb.append("📅 Kỳ phân tích: ").append(start.format(fmt)).append(" - ").append(end.format(fmt)).append("\n");
        sb.append("💰 Doanh thu kỳ này: ").append(String.format("%,.0f", current)).append(" VNĐ\n");
        sb.append("💰 Doanh thu kỳ trước: ").append(String.format("%,.0f", previous)).append(" VNĐ\n");

        if (previous.compareTo(BigDecimal.ZERO) > 0) {
            double change = current.subtract(previous)
                    .divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            sb.append("📈 Tăng/Giảm: ").append(String.format("%.1f", change)).append("%\n\n");
        }

        sb.append("🏆 Top sản phẩm bán chạy:\n");
        if (topProducts.isEmpty()) {
            sb.append("- Chưa có dữ liệu\n");
        } else {
            for (int i = 0; i < Math.min(5, topProducts.size()); i++) {
                Object[] row = topProducts.get(i);
                sb.append(i + 1).append(". ").append(row[0])
                  .append(" - ").append(row[1]).append(" ly")
                  .append(" - ").append(String.format("%,.0f", row[2])).append(" VNĐ\n");
            }
        }

        sb.append("\n📊 Doanh thu theo ngày:\n");
        if (dailyRevenue.isEmpty()) {
            sb.append("- Chưa có dữ liệu\n");
        } else {
            for (Object[] row : dailyRevenue) {
                sb.append("- ").append(row[0]).append(": ")
                  .append(String.format("%,.0f", row[1])).append(" VNĐ\n");
            }
        }

        sb.append("\nQUY TẮC ĐỊNH DẠNG BẮT BUỘC:\n");
        sb.append("Hãy cấu trúc báo cáo thành ĐÚNG 3 PHẦN RIÊNG BIỆT với tiêu đề Markdown cấp 3 (###) như sau:\n");
        sb.append("### 🟢 1. ĐIỂM SÁNG DOANH THU & SẢN PHẨM BÁN CHẠY\n");
        sb.append("- Nhận xét tổng quan doanh thu kỳ này so với kỳ trước (nêu rõ tỷ lệ % tăng/giảm).\n");
        sb.append("- Chỉ ra các món đồ uống 'gánh team' bán chạy nhất và ngày có doanh số cao nhất.\n\n");
        sb.append("### 🔴 2. CẢNH BÁO RỦI RO & MÓN CẦN ĐẨY MẠNH\n");
        sb.append("- Cảnh báo ngày có doanh thu sụt giảm hoặc khung giờ vắng khách.\n");
        sb.append("- Nhận diện các món bán chậm có nguy cơ tồn đọng vốn hoặc cần làm mới.\n\n");
        sb.append("### 🎯 3. BA HÀNH ĐỘNG CỤ THỂ CHO QUẢN LÝ TUẦN NÀY\n");
        sb.append("- Đưa ra đúng 3 gạch đầu dòng hành động thiết thực (ví dụ: tạo combo Cà phê/Trà kết hợp Đồ ăn vặt Hạt hướng dương / Hạt dưa / Khô gà để tăng doanh số bàn, điều chỉnh nhân sự ca trực, chạy ưu đãi giờ thấp điểm).\n");
        sb.append("LƯU Ý: Trình bày súc tích, gạch đầu dòng rõ ràng, dùng emoji sinh động, tuyệt đối KHÔNG viết đoạn văn xuôi dài dòng lê thê.");

        return sb.toString();
    }

    /**
     * AI Đánh giá Tồn kho & Dự trù Kế hoạch Nhập hàng
     */
    public String analyzeInventory() {
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY") || apiKey.isBlank() || apiKey.length() < 10) {
            return "⚠️ Chưa cấu hình Gemini API Key. Vui lòng điền key vào application.properties.";
        }

        List<Ingredient> lowStock = ingredientService.findLowStock();
        LocalDateTime weekStart = LocalDateTime.now().minusDays(6).with(LocalTime.MIN);
        List<Object[]> topProducts = invoiceService.getTopSellingProducts(weekStart, LocalDateTime.now());

        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là chuyên gia quản lý kho và chuỗi cung ứng của quán cà phê Coffee Mango.\n");
        sb.append("QUY TẮC BẮT BUỘC: Hãy trả lời 100% bằng TIẾNG VIỆT tự nhiên, súc tích, chuyên nghiệp. Tuyệt đối KHÔNG dùng tiếng Anh.\n\n");

        sb.append("📦 DANH SÁCH NGUYÊN LIỆU ĐANG SẮP HẾT (CHẠM MỨC BÁO ĐỘNG):\n");
        if (lowStock.isEmpty()) {
            sb.append("- Hiện tại tất cả nguyên liệu trong kho đều ở mức an toàn.\n");
        } else {
            for (Ingredient ing : lowStock) {
                sb.append("- ").append(ing.getName()).append(": Còn ").append(ing.getFormattedQuantity())
                  .append(" ").append(ing.getUnit()).append(" (Mức an toàn tối thiểu: ").append(ing.getFormattedMinQuantity()).append(" ").append(ing.getUnit()).append(")\n");
            }
        }

        sb.append("\n🏆 TOP MÓN ĐANG BÁN CHẠY TRONG TUẦN NÀY:\n");
        if (topProducts.isEmpty()) {
            sb.append("- Chưa có dữ liệu bán hàng tuần này\n");
        } else {
            for (int i = 0; i < Math.min(5, topProducts.size()); i++) {
                Object[] row = topProducts.get(i);
                sb.append("- ").append(row[0]).append(": ").append(row[1]).append(" ly\n");
            }
        }

        sb.append("\nYêu cầu đánh giá (bằng 100% tiếng Việt, dùng emoji, trình bày có cấu trúc rõ ràng, ngắn gọn):\n");
        sb.append("1. Đánh giá mức độ cấp bách của kho nguyên liệu\n");
        sb.append("2. Cảnh báo rủi ro đứt hàng đối với các món đồ uống chủ lực\n");
        sb.append("3. Đề xuất số lượng nhập hàng ưu tiên cụ thể cho ca trực tiếp theo\n");
        sb.append("4. Mẹo bảo quản hoặc giảm hao hụt nguyên liệu");

        return callGeminiAPI(sb.toString());
    }

    @SuppressWarnings("unchecked")
    private String callGeminiAPI(String prompt) {
        List<String> targetUrls = new ArrayList<>();
        if (apiUrl != null && !apiUrl.isBlank()) {
            targetUrls.add(apiUrl);
        }

        // Danh sách endpoint dự phòng (fallback) khi một model bị 503 hoặc quá tải
        String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/";
        String[] fallbackModels = {
            "gemini-flash-lite-latest",
            "gemini-3.5-flash-lite",
            "gemini-3.5-flash",
            "gemini-3.6-flash"
        };
        for (String model : fallbackModels) {
            String url = baseUrl + model + ":generateContent";
            if (!targetUrls.contains(url)) {
                targetUrls.add(url);
            }
        }

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ),
            "generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 2048
            )
        );

        WebClientResponseException lastWebException = null;
        Exception lastException = null;

        for (int i = 0; i < targetUrls.size(); i++) {
            String url = targetUrls.get(i);
            try {
                Map<String, Object> response = webClient.post()
                        .uri(url)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                if (response != null && response.containsKey("candidates")) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            StringBuilder textBuilder = new StringBuilder();
                            for (Map<String, Object> part : parts) {
                                if (part.containsKey("text") && part.get("text") != null) {
                                    textBuilder.append(part.get("text").toString());
                                }
                            }
                            if (textBuilder.length() > 0) {
                                return textBuilder.toString();
                            }
                        }
                    }
                }
            } catch (WebClientResponseException e) {
                lastWebException = e;
                int status = e.getStatusCode().value();
                // Nếu bị 503 (quá tải), 429 (rate limit) hoặc 404/500 -> tự động chuyển model tiếp theo
                if ((status == 503 || status == 429 || status == 404 || status == 500) && i < targetUrls.size() - 1) {
                    continue;
                }
                break;
            } catch (Exception e) {
                lastException = e;
                if (i < targetUrls.size() - 1) {
                    continue;
                }
                break;
            }
        }

        if (lastWebException != null) {
            int status = lastWebException.getStatusCode().value();
            if (status == 503) {
                return "⏳ Máy chủ Google AI hiện đang quá tải tạm thời (503 Service Unavailable). Hệ thống đã tự động thử các kênh dự phòng nhưng máy chủ Google đang nghẽn lưu lượng. Bạn vui lòng đợi vài giây rồi thử hỏi lại nhé!";
            }
            if (status == 429) {
                return "⏳ AI đang bận (429 Too Many Requests). Hệ thống đã chạm giới hạn gọi miễn phí trong phút này, vui lòng thử lại sau ít giây nhé!";
            }
            if (status == 401) {
                return "🔑 API Key không hợp lệ. Vui lòng kiểm tra lại cấu hình gemini.api.key.";
            }
            if (status == 404) {
                return "❌ Model AI không tồn tại hoặc đã ngừng hỗ trợ (404).";
            }
            return "❌ Lỗi kết nối Google AI (" + status + "). Vui lòng thử lại sau ít giây.";
        }

        if (lastException != null) {
            return "❌ Lỗi: " + lastException.getMessage();
        }

        return "Không thể lấy kết quả từ AI. Vui lòng thử lại.";
    }
}
