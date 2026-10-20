# ☕ Coffee Mango - Hệ thống quản lý quán cà phê tích hợp AI

## Tech Stack
- **Backend:** Java 17 + Spring Boot 3.2
- **Frontend:** Thymeleaf + Bootstrap 5
- **Database:** MySQL 8
- **AI:** Google Gemini API

## Cấu trúc MVC

```
src/main/java/com/coffeemango/
├── model/              ← Entity (Model)
│   ├── User.java
│   ├── Category.java
│   ├── Product.java
│   ├── Ingredient.java
│   ├── Invoice.java
│   ├── InvoiceDetail.java
│   └── Role.java
├── repository/         ← Data Access Layer
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── ProductRepository.java
│   ├── IngredientRepository.java
│   ├── InvoiceRepository.java
│   └── InvoiceDetailRepository.java
├── service/            ← Business Logic
│   ├── UserService.java
│   ├── CategoryService.java
│   ├── ProductService.java
│   ├── IngredientService.java
│   ├── InvoiceService.java
│   ├── GeminiAIService.java          ← AI Service
│   └── CustomUserDetailsService.java
├── controller/         ← Controller
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── IngredientController.java
│   ├── UserController.java
│   ├── InvoiceController.java
│   └── RevenueController.java        ← AI endpoint
└── config/
    ├── SecurityConfig.java
    ├── WebConfig.java
    └── DataInitializer.java          ← Dữ liệu mẫu

src/main/resources/
├── templates/          ← Thymeleaf Views
│   ├── layout/         ← Sidebar, topnav dùng chung
│   ├── auth/           ← Trang đăng nhập
│   ├── dashboard/
│   ├── products/
│   ├── categories/
│   ├── ingredients/
│   ├── staff/
│   ├── invoices/
│   └── revenue/        ← Trang thống kê + AI
└── static/
    ├── css/style.css
    └── js/main.js
```

## Cài đặt và chạy

### 1. Tạo database
```sql
CREATE DATABASE coffeemango CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Cấu hình application.properties
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
gemini.api.key=YOUR_GEMINI_API_KEY
```

### 3. Lấy Gemini API Key
- Truy cập: https://aistudio.google.com/apikey
- Tạo API Key miễn phí

### 4. Chạy ứng dụng
```bash
cd coffeemango
mvn spring-boot:run
```

### 5. Truy cập
- URL: http://localhost:8080
- Admin: `admin / admin123`
- Manager: `manager / manager123`
- Staff: `nhanvien1 / staff123`

## Tính năng

| Chức năng | Mô tả |
|-----------|-------|
| Đăng nhập | Spring Security, phân quyền 3 cấp |
| Dashboard | Tổng quan doanh thu, cảnh báo nguyên liệu |
| Sản phẩm | CRUD, tìm kiếm, ẩn/hiện |
| Danh mục | CRUD, soft delete |
| Nguyên liệu | CRUD, cảnh báo sắp hết |
| Nhân viên | CRUD, phân quyền (Admin only) |
| Hóa đơn | Tạo, xem, hoàn thành, hủy |
| Thống kê | Doanh thu theo kỳ, top sản phẩm |
| 🤖 AI | Phân tích doanh thu bằng Gemini API |
