package com.example.QLPT.Funsion.landlady.Manage_User;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manage_User_DTO {
    private Long id;
    @Size(max = 255)
    private String fullName;
    @Email @Size(max = 255)
    private String email;
    @Size(max = 255)
    private String phoneNumber;
    @Size(max = 255)
    private String identityCard;
    private String role;
    private String status;

    private List<RentedRoomInfo> rentedRooms;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RentedRoomInfo {
        private Long roomId;
        private String propertyName;
        private String roomNumber;
        private BigDecimal price;
        private Boolean isPaidThisMonth;
    }

    // --- MỚI: Lớp chứa thông tin Lịch sử Khách Thuê ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TenantHistory {
        private Long contractId;
        private String propertyName;
        private String roomNumber;
        private LocalDate startDate;
        private LocalDate endDate;
        private String contractStatus; // ACTIVE, EXPIRED, TERMINATED
        private BigDecimal depositAmount;

        // Tổng hợp hóa đơn trong hợp đồng này
        private int totalInvoices;
        private int unpaidInvoices;
        private BigDecimal totalPaidAmount;
        private BigDecimal totalUnpaidAmount;
    }
}