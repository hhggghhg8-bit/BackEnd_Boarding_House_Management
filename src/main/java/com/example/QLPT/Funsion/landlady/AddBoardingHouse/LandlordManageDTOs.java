package com.example.QLPT.Funsion.landlady.AddBoardingHouse;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LandlordManageDTOs {

    // 1. DTO Thêm Khu trọ
    @Data
    public static class CreatePropertyDTO {
        @NotBlank @Size(max = 255)
        private String name;
        @NotBlank @Size(max = 255)
        private String address;
        private String description;
        private String rules;
        @Min(1) @Max(31)
        private Integer billingDay;
        @Min(1) @Max(31)
        private Integer paymentDeadlineDay;
    }

    // 2. DTO Thêm Phòng trọ
    @Data
    public static class CreateRoomDTO {
        @NotNull @Positive
        private Long propertyId; // Bắt buộc phải biết thêm vào khu trọ nào
        @NotBlank @Size(max = 255)
        private String roomNumber;
        @Positive
        private BigDecimal area;
        @Positive
        private Integer maxCapacity;
        @NotNull @PositiveOrZero
        private BigDecimal basePrice;
        private String amenities; // VD: "Điều hòa, Nóng lạnh"
    }

    // 3. DTO Thêm Người thuê (Tạo hợp đồng)
    @Data
    public static class AssignTenantDTO {
        @NotNull @Positive
        private Long roomId;          // ID của phòng trống
        @NotBlank @Email @Size(max = 255)
        private String tenantEmail;   // Email của người thuê (Họ phải đăng ký tài khoản trước)
        private LocalDate startDate;  // Ngày bắt đầu thuê
        private LocalDate endDate;    // Ngày hết hạn hợp đồng
        @PositiveOrZero
        private BigDecimal deposit;   // Tiền cọc
    }
    // 4. DTO Thêm Nhiều Phòng Cùng Lúc (Theo tầng)
    @Data
    public static class CreateMultipleRoomsDTO {
        @NotNull @Positive
        private Long propertyId;
        @NotNull @Min(1) @Max(100)
        private Integer numberOfFloors; // Số lượng tầng (VD: 2)
        @NotNull @Min(1) @Max(99)
        private Integer roomsPerFloor;  // Số phòng mỗi tầng (VD: 5)

        // Các thông tin chung áp dụng cho tất cả các phòng này
        @Positive
        private BigDecimal area;
        @Positive
        private Integer maxCapacity;
        @NotNull @PositiveOrZero
        private BigDecimal basePrice;
        private String amenities;
    }
    @Data
    public static class UpdateRoomDTO {
        @Size(max = 255)
        private String roomNumber;
        @Positive
        private BigDecimal area;
        @Positive
        private Integer maxCapacity;
        @PositiveOrZero
        private BigDecimal basePrice;
        private String amenities;
        private String status; // Cho phép chủ trọ tự chỉnh trạng thái (AVAILABLE, RENTED, MAINTENANCE)
    }
}
