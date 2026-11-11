package com.example.QLPT.Funsion.User.total;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UserDTO {

    // Thông tin tổng quan về phòng đang thuê
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MyRoomResponse {
        private Long roomId;
        private String propertyName;
        private String roomNumber;
        private String propertyAddress;
        private BigDecimal basePrice;
    }

    // Lịch sử hóa đơn của khách
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MyInvoiceResponse {
        private Long invoiceId;
        private String billingCycle;
        private LocalDate dueDate;
        private BigDecimal totalAmount;
        private String status; // UNPAID, PAID
        private String meterImageUrl; // Khách có thể xem ảnh bằng chứng

        // Chi tiết sử dụng
        private Integer oldElectricity;
        private Integer newElectricity;
        private Integer electricUsage;
        private BigDecimal electricTotal;

        private Integer oldWater;
        private Integer newWater;
        private Integer waterUsage;
        private BigDecimal waterTotal;

        // Danh sách các dịch vụ khác (Wifi, Vệ sinh, rác...)
        private List<ServiceDetail> extraServices;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ServiceDetail {
        private String serviceName;
        private BigDecimal appliedPrice;
    }
    // THÊM ĐOẠN NÀY ĐỂ HỨNG TOKEN TỪ APP GỬI LÊN
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class FcmTokenRequest {
        private String fcmToken;
    }
}