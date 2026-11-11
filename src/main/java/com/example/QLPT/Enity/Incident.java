package com.example.QLPT.Enity;

import com.example.QLPT.Enity.Enum.Enum;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Incident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String attachmentUrl;

    @Column(columnDefinition = "TEXT")
    private String landlordNote;

    @Enumerated(EnumType.STRING)
    private Enum.IncidentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
