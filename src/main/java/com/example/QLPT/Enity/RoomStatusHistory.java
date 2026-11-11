package com.example.QLPT.Enity;

import com.example.QLPT.Enity.Enum.Enum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_status_histories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    private Enum.RoomStatus previousStatus;

    @Enumerated(EnumType.STRING)
    private Enum.RoomStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    // Bảng history (lịch sử) chỉ lưu log thêm mới, không bao giờ update nên không cần updatedAt
}