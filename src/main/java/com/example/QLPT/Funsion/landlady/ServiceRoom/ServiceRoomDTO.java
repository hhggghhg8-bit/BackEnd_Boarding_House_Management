package com.example.QLPT.Funsion.landlady.ServiceRoom;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ServiceRoomDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CatalogResponse {
        private Long id;
        private String name;
        private String unit;
        private BigDecimal defaultPrice;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateCatalogRequest {
        @NotBlank @Size(max = 255)
        private String name;
        @NotBlank @Size(max = 255)
        private String unit;
        @NotNull @PositiveOrZero
        private BigDecimal defaultPrice;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RoomServiceResponse {
        private Long roomServiceId;
        private Long serviceId;
        private String serviceName;
        private String unit;
        private BigDecimal defaultPrice;
        private BigDecimal customPrice;
        private BigDecimal appliedPrice;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RoomBillingSummary {
        private Long roomId;
        private String propertyName;
        private String roomNumber;
        private String tenantName;
        private String tenantPhone;
        private Boolean isPaidThisMonth;
        private String billingCycle;
        private BigDecimal totalAmount;

        // --- Dữ liệu chi tiết cho tháng hiện tại ---
        private Integer electricUsage;
        private BigDecimal electricTotal;
        private Integer waterUsage;
        private BigDecimal waterTotal;

        // --- MỚI: Trả về số cũ (tháng trước) để Frontend tự động điền ---
        private Integer lastElectricity;
        private Integer lastWater;

        private java.util.List<RoomServiceResponse> services;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AssignRequest {
        @NotNull @Positive
        private Long serviceId;
        @PositiveOrZero
        private BigDecimal customPrice;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RecordUtilityRequest {
        @NotBlank @Pattern(regexp = "(0[1-9]|1[0-2])-[0-9]{4}")
        private String billingCycle;
        private LocalDate readingDate;
        private LocalDate dueDate;
        @PositiveOrZero
        private BigDecimal newRoomPrice;
        @PositiveOrZero
        private BigDecimal newElecPrice;
        @PositiveOrZero
        private BigDecimal newWaterPrice;
        @NotNull @PositiveOrZero
        private Integer oldElectricity;
        @NotNull @PositiveOrZero
        private Integer newElectricity;
        @NotNull @PositiveOrZero
        private Integer oldWater;
        @NotNull @PositiveOrZero
        private Integer newWater;
        private Boolean isMeterReset;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UtilityReadingResponse {
        private String billingCycle;
        private Integer electricUsage;
        private BigDecimal electricTotal;
        private Integer waterUsage;
        private BigDecimal waterTotal;
        private BigDecimal grandTotal;
        private String meterImageUrl;
    }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InvoiceHistoryResponse {
        private Long invoiceId;
        private String billingCycle;
        private LocalDate readingDate;
        private LocalDate dueDate;
        private BigDecimal totalAmount;
        private String status; // UNPAID, PAID
        private String meterImageUrl; // Ảnh bằng chứng chốt số

        // Chi tiết để hiển thị thêm nếu cần
        private Integer electricUsage;
        private Integer waterUsage;
    }
}