package com.coffeemango.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coffee_tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoffeeTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "table_number", nullable = false, unique = true, length = 20)
    private String tableNumber;
    
    @Column(nullable = false, length = 100)
    private String location; // "Trong nhà" hoặc "Ngoài sân"
    
    @Column(nullable = false)
    private Integer capacity; // Sức chứa (số người)
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TableStatus status = TableStatus.AVAILABLE;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TableStatus {
        AVAILABLE,   // Bàn trống
        OCCUPIED,    // Đang có khách
        RESERVED     // Đã đặt trước
    }
}
