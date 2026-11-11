package com.example.QLPT.Enity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "landlord_bank_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LandlordBankAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String bankName;
    private String accountNumber;
    private String accountHolderName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}