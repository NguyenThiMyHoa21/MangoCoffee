package com.coffeemango.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CoffeeTable coffeeTable; // Bàn phục vụ

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(length = 50)
    private String paymentMethod; // CASH, CARD, TRANSFER

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(length = 255)
    private String note;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<InvoiceDetail> details;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum InvoiceStatus {
        PENDING, COMPLETED, CANCELLED
    }
}
