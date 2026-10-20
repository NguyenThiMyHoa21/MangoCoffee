package com.coffeemango.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity; // số lượng tồn kho

    @Column(nullable = false, length = 20)
    private String unit; // kg, lít, cái...

    @Column(precision = 10, scale = 2)
    private BigDecimal minQuantity; // mức tối thiểu cần nhập thêm

    @Column(precision = 10, scale = 2)
    private BigDecimal costPerUnit; // giá nhập / đơn vị

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BigDecimal getUnitPrice() {
        return costPerUnit;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.costPerUnit = unitPrice;
    }

    public String getFormattedQuantity() {
        if (quantity == null) return "0";
        if (quantity.compareTo(BigDecimal.ZERO) == 0) return "0";
        return quantity.stripTrailingZeros().toPlainString();
    }

    public String getFormattedMinQuantity() {
        if (minQuantity == null) return "—";
        if (minQuantity.compareTo(BigDecimal.ZERO) == 0) return "0";
        return minQuantity.stripTrailingZeros().toPlainString();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
