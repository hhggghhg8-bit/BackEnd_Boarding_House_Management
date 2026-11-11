package com.example.QLPT.Enity;

import com.example.QLPT.Enity.Enum.Enum;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_histories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContractHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    @Enumerated(EnumType.STRING)
    private Enum.HistoryAction action;

    @Column(columnDefinition = "TEXT")
    private String previousState; // Lưu chuỗi JSON

    @Column(columnDefinition = "TEXT")
    private String newState; // Lưu chuỗi JSON

    private String reason;
    private LocalDateTime createdAt;
}