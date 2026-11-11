package com.example.QLPT.Enity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "utility_readings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UtilityReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private String billingCycle;
    private LocalDate readingDate;

    private Integer oldElectricity;
    private Integer newElectricity;
    private Integer oldWater;
    private Integer newWater;

    private Boolean isMeterReset;
    private String meterImageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
