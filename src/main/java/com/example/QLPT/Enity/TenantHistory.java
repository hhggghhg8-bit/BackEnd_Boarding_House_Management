package com.example.QLPT.Enity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_histories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private LocalDate moveInDate;
    private LocalDate moveOutDate;
    private Boolean isPrimaryTenant;

    private LocalDateTime createdAt;
}
