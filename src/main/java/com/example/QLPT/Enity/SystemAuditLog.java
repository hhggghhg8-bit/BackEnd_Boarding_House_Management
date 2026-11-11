package com.example.QLPT.Enity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") // Có thể null nếu hệ thống tự động chạy
    private User user;

    private String actionType;
    private String tableName;
    private Long recordId;
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String payload; // Lưu chuỗi JSON

    private LocalDateTime createdAt;
}
