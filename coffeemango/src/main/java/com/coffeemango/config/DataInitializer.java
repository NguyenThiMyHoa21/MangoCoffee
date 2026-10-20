package com.coffeemango.config;

import com.coffeemango.model.*;
import com.coffeemango.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final InvoiceRepository invoiceRepository;
    private final com.coffeemango.repository.CoffeeTableRepository tableRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // ===== USERS =====
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder().username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên").email("admin@coffeemango.com")
                    .role(Role.ADMIN).active(true).build());
            userRepository.save(User.builder().username("manager")
                    .password(passwordEncoder.encode("manager123"))
                    .fullName("Nguyễn Văn Quản Lý").email("manager@coffeemango.com")
                    .role(Role.MANAGER).active(true).build());
            userRepository.save(User.builder().username("nhanvien1")
                    .password(passwordEncoder.encode("staff123"))
                    .fullName("Trần Thị Nhân Viên").email("nhanvien1@coffeemango.com")
                    .role(Role.STAFF).active(true).build());
        }

        // ===== CATEGORIES & PRODUCTS =====
        if (categoryRepository.count() == 0) {
            Category coffee = categoryRepository.save(Category.builder()
                    .name("Cà phê").description("Các loại cà phê").active(true).build());
            Category tea = categoryRepository.save(Category.builder()
                    .name("Trà").description("Các loại trà").active(true).build());
            Category juice = categoryRepository.save(Category.builder()
                    .name("Nước ép").description("Nước ép trái cây").active(true).build());
            Category snack = categoryRepository.save(Category.builder()
                    .name("Đồ ăn vặt").description("Hạt hướng dương, hạt dưa, khô gà ăn kèm").active(true).build());

            productRepository.save(Product.builder().name("Cà phê đen")
                    .price(new BigDecimal("25000")).category(coffee).available(true)
                    .description("Cà phê đen truyền thống").build());
            productRepository.save(Product.builder().name("Cà phê sữa")
                    .price(new BigDecimal("30000")).category(coffee).available(true)
                    .description("Cà phê sữa thơm ngon").build());
            productRepository.save(Product.builder().name("Cappuccino")
                    .price(new BigDecimal("55000")).category(coffee).available(true)
                    .description("Cappuccino Ý").build());
            productRepository.save(Product.builder().name("Latte")
                    .price(new BigDecimal("55000")).category(coffee).available(true)
                    .description("Latte mịn màng").build());
            productRepository.save(Product.builder().name("Trà đào")
                    .price(new BigDecimal("45000")).category(tea).available(true)
                    .description("Trà đào cam sả").build());
            productRepository.save(Product.builder().name("Trà sữa trân châu")
                    .price(new BigDecimal("50000")).category(tea).available(true)
                    .description("Trà sữa trân châu đen").build());
            productRepository.save(Product.builder().name("Nước ép cam")
                    .price(new BigDecimal("40000")).category(juice).available(true)
                    .description("Nước ép cam tươi").build());
            productRepository.save(Product.builder().name("Hạt hướng dương")
                    .price(new BigDecimal("15000")).category(snack).available(true)
                    .description("Hạt hướng dương thơm giòn vị dừa").build());
            productRepository.save(Product.builder().name("Hạt dưa")
                    .price(new BigDecimal("20000")).category(snack).available(true)
                    .description("Hạt dưa rang giòn nhâm nhi").build());
            productRepository.save(Product.builder().name("Khô gà lá chanh")
                    .price(new BigDecimal("30000")).category(snack).available(true)
                    .description("Khô gà cay nhẹ thơm lá chanh").build());
        }

        // Tự động chuẩn hóa nếu còn danh mục Bánh
        categoryRepository.findAll().forEach(cat -> {
            if ("Bánh".equalsIgnoreCase(cat.getName())) {
                cat.setName("Đồ ăn vặt");
                cat.setDescription("Hạt hướng dương, hạt dưa, khô gà ăn kèm");
                categoryRepository.save(cat);
            }
        });

        // ===== INGREDIENTS =====
        initIngredients();

        // ===== TABLES =====
        if (tableRepository.count() == 0) {
            String[] locations = {"Trong nhà", "Trong nhà", "Trong nhà", "Trong nhà",
                                  "Trong nhà", "Ngoài trời", "Ngoài trời", "Ngoài trời",
                                  "Tầng 2", "Tầng 2"};
            int[] capacities = {2, 2, 4, 4, 4, 4, 4, 6, 4, 6};
            for (int i = 1; i <= 10; i++) {
                tableRepository.save(com.coffeemango.model.CoffeeTable.builder()
                        .tableNumber("Bàn " + String.format("%02d", i))
                        .capacity(capacities[i-1])
                        .location(locations[i-1])
                        .status(com.coffeemango.model.CoffeeTable.TableStatus.AVAILABLE)
                        .build());
            }
        }

        // ===== INVOICE SAMPLE DATA =====
        if (invoiceRepository.count() == 0) {
            List<Product> products = productRepository.findAll();
            User staff = userRepository.findByUsername("nhanvien1").orElse(null);
            Random random = new Random(42);
            String[] paymentMethods = {"CASH", "CARD", "TRANSFER"};
            int invoiceNum = 1;

            // Tạo hóa đơn cho 30 ngày gần nhất
            for (int day = 29; day >= 0; day--) {
                // Mỗi ngày 5-15 hóa đơn
                int ordersPerDay = 5 + random.nextInt(11);
                // Cuối tuần nhiều hơn
                LocalDateTime date = LocalDateTime.now().minusDays(day);
                int dayOfWeek = date.getDayOfWeek().getValue();
                if (dayOfWeek == 6 || dayOfWeek == 7) ordersPerDay += 5;

                for (int o = 0; o < ordersPerDay; o++) {
                    Invoice invoice = new Invoice();
                    invoice.setInvoiceCode(String.format("INV-%06d", invoiceNum++));
                    invoice.setStaff(staff);
                    invoice.setStatus(Invoice.InvoiceStatus.COMPLETED);
                    invoice.setPaymentMethod(paymentMethods[random.nextInt(3)]);
                    invoice.setDiscount(BigDecimal.ZERO);

                    // Mỗi hóa đơn 1-4 sản phẩm
                    int itemCount = 1 + random.nextInt(4);
                    List<InvoiceDetail> details = new ArrayList<>();
                    BigDecimal total = BigDecimal.ZERO;

                    for (int i = 0; i < itemCount; i++) {
                        Product product = products.get(random.nextInt(products.size()));
                        int qty = 1 + random.nextInt(3);
                        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
                        total = total.add(subtotal);

                        InvoiceDetail detail = new InvoiceDetail();
                        detail.setInvoice(invoice);
                        detail.setProduct(product);
                        detail.setQuantity(qty);
                        detail.setUnitPrice(product.getPrice());
                        detail.setSubtotal(subtotal);
                        details.add(detail);
                    }

                    invoice.setTotalAmount(total);
                    invoice.setFinalAmount(total);
                    invoice.setDetails(details);

                    // Set thời gian ngẫu nhiên trong ngày (7h-22h)
                    int hour = 7 + random.nextInt(15);
                    int minute = random.nextInt(60);
                    LocalDateTime invoiceTime = date.withHour(hour).withMinute(minute).withSecond(0);
                    invoice.setCreatedAt(invoiceTime);
                    invoice.setUpdatedAt(invoiceTime);

                    invoiceRepository.save(invoice);
                }
            }
        }
    }

    private void initIngredients() {
        saveIngredientIfAbsent("Cà phê hạt Robusta Đắk Lắk", new BigDecimal("12.5"), "kg", new BigDecimal("5.0"), new BigDecimal("185000"));
        saveIngredientIfAbsent("Cà phê hạt Arabica Cầu Đất", new BigDecimal("8.0"), "kg", new BigDecimal("3.0"), new BigDecimal("260000"));
        saveIngredientIfAbsent("Sữa đặc có đường Phương Nam", new BigDecimal("15.0"), "hộp", new BigDecimal("6.0"), new BigDecimal("24000"));
        saveIngredientIfAbsent("Sữa tươi Barista không đường", new BigDecimal("2.5"), "lít", new BigDecimal("8.0"), new BigDecimal("32000"));
        saveIngredientIfAbsent("Đường cát trắng tinh luyện", new BigDecimal("18.0"), "kg", new BigDecimal("5.0"), new BigDecimal("22000"));
        saveIngredientIfAbsent("Đá viên tinh khiết", new BigDecimal("4.0"), "kg", new BigDecimal("15.0"), new BigDecimal("6000"));
        saveIngredientIfAbsent("Trà hoa lài thượng hạng", new BigDecimal("6.0"), "kg", new BigDecimal("2.0"), new BigDecimal("190000"));
        saveIngredientIfAbsent("Đào ngâm đóng hộp Hosen", new BigDecimal("2.0"), "hộp", new BigDecimal("5.0"), new BigDecimal("45000"));
        saveIngredientIfAbsent("Cam sành Hàm Yên tươi", new BigDecimal("3.0"), "kg", new BigDecimal("6.0"), new BigDecimal("45000"));
        saveIngredientIfAbsent("Trân châu đen Đài Loan", new BigDecimal("9.0"), "kg", new BigDecimal("4.0"), new BigDecimal("48000"));
        saveIngredientIfAbsent("Bột Cacao nguyên chất Đắk Lắk", new BigDecimal("4.5"), "kg", new BigDecimal("2.0"), new BigDecimal("140000"));
        saveIngredientIfAbsent("Bột Matcha Uji Nhật Bản", new BigDecimal("1.2"), "kg", new BigDecimal("1.0"), new BigDecimal("450000"));
        saveIngredientIfAbsent("Syrup Caramel Monin", new BigDecimal("3.0"), "chai", new BigDecimal("1.0"), new BigDecimal("230000"));
        saveIngredientIfAbsent("Kem béo thực vật Rich's Lùn", new BigDecimal("6.0"), "hộp", new BigDecimal("3.0"), new BigDecimal("32000"));
        saveIngredientIfAbsent("Hạt hướng dương rang dừa", new BigDecimal("15.0"), "gói", new BigDecimal("5.0"), new BigDecimal("12000"));
        saveIngredientIfAbsent("Hạt dưa rang muối", new BigDecimal("12.0"), "gói", new BigDecimal("5.0"), new BigDecimal("14000"));
        saveIngredientIfAbsent("Khô gà lá chanh cay xé sợi", new BigDecimal("10.0"), "hộp", new BigDecimal("4.0"), new BigDecimal("22000"));
        saveIngredientIfAbsent("Bò khô sợi Đà Nẵng", new BigDecimal("6.0"), "hộp", new BigDecimal("3.0"), new BigDecimal("35000"));
        saveIngredientIfAbsent("Ly nhựa nắp cầu 500ml", new BigDecimal("350"), "cái", new BigDecimal("150"), new BigDecimal("800"));
        saveIngredientIfAbsent("Ống hút gạo tự hủy", new BigDecimal("400"), "cái", new BigDecimal("150"), new BigDecimal("350"));
    }

    private void saveIngredientIfAbsent(String name, BigDecimal qty, String unit, BigDecimal minQty, BigDecimal cost) {
        if (!ingredientRepository.existsByName(name)) {
            ingredientRepository.save(Ingredient.builder()
                    .name(name)
                    .quantity(qty)
                    .unit(unit)
                    .minQuantity(minQty)
                    .costPerUnit(cost)
                    .build());
        }
    }
}
