package com.example.QLPT.Enity;

import com.example.QLPT.Enity.Enum.Enum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
// Bật tính năng Xóa mềm (Soft Delete)
@SQLDelete(sql = "UPDATE rooms SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dùng LAZY để tránh phình to bộ nhớ khi query
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    private String roomNumber;
    private BigDecimal area;
    private Integer maxCapacity;
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String amenities; // Lưu chuỗi JSON, ví dụ: '["Điều hòa", "Máy giặt"]'

    @Enumerated(EnumType.STRING)
    private Enum.RoomStatus status;

    @CreationTimestamp
    @Column(updatable = false) // Không cho phép update lại ngày tạo
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}