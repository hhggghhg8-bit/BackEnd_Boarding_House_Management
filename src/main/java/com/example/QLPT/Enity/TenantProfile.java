package com.example.QLPT.Enity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate dateOfBirth;
    private String hometown;
    private String occupation;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
