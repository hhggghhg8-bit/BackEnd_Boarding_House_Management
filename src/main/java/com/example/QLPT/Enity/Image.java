package com.example.QLPT.Enity;

import com.example.QLPT.Enity.Enum.Enum;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Image {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private Enum.ReferenceType referenceType; // PROPERTY hoặc ROOM

    private String imageUrl;
    private LocalDateTime createdAt;
}
