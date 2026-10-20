package com.coffeemango.service;

import com.coffeemango.model.User;
import com.coffeemango.model.Role;
import com.coffeemango.repository.InvoiceRepository;
import com.coffeemango.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findActiveStaff() {
        return userRepository.findByActive(true);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User save(User user) {
        if (user.getId() == null) {
            // Mã hóa mật khẩu khi tạo mới
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User update(Long id, User updatedUser) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
        existing.setFullName(updatedUser.getFullName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPhone(updatedUser.getPhone());
        existing.setRole(updatedUser.getRole());
        existing.setActive(updatedUser.isActive());
        return userRepository.save(existing);
    }

    public void changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Không thể xóa tài khoản Quản trị viên (Admin) gốc!");
        }
        // Gỡ liên kết nhân viên khỏi các hóa đơn trước đó để không bị lỗi khóa ngoại
        invoiceRepository.unlinkStaffFromInvoices(id);
        // Xóa hoàn toàn khỏi database
        userRepository.delete(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
