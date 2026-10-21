package com.coffeemango.config;

import com.coffeemango.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    // =========================================================================================
    // [VẤN ĐÁP - CHỖ 1]: MÃ HÓA MẬT KHẨU BẰNG THUẬT TOÁN BCRYPT
    // -----------------------------------------------------------------------------------------
    // - Dùng BCryptPasswordEncoder mã hóa một chiều an toàn với Salt ngẫu nhiên (độ phức tạp 10).
    // - Ngăn chặn tấn công tra cứu bảng Rainbow Table, mật khẩu trong MySQL luôn là chuỗi băm.
    // =========================================================================================
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    // =========================================================================================
    // [VẤN ĐÁP - CHỖ 2]: PHÂN QUYỀN URL DỰA TRÊN VAI TRÒ (RBAC - ROLE-BASED ACCESS CONTROL)
    // -----------------------------------------------------------------------------------------
    // - /staff/**, /tables/manage/**       -> Chỉ ADMIN (Chủ quán mới quản lý nhân sự & cấu hình bàn)
    // - /revenue/**                        -> Chỉ ADMIN, MANAGER (Nhân viên STAFF bị chặn 403 Forbidden)
    // - /products/**, /ingredients/**      -> ADMIN, MANAGER mới được Thêm/Sửa/Xóa
    // - /ai/**, /dashboard, /invoices/**   -> Cả 3 Role đều được dùng (STAFF phục vụ bán hàng POS)
    // =========================================================================================
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/staff/**").hasRole("ADMIN")
                .requestMatchers("/tables/manage", "/tables/manage/**").hasRole("ADMIN")
                .requestMatchers("/revenue/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/products/create", "/products/edit/**", "/products/delete/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/categories/create", "/categories/edit/**", "/categories/delete/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/ingredients/create", "/ingredients/edit/**", "/ingredients/delete/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/ai/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
