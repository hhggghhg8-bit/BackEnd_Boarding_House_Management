package com.example.QLPT.Enity;


import com.example.QLPT.Enity.Enum.Enum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualEndDate;
    private Integer paymentDayOverride;

    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    private Enum.DepositStatus depositStatus;

    private String contractImageUrl;

    @Enumerated(EnumType.STRING)
    private Enum.ContractStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
