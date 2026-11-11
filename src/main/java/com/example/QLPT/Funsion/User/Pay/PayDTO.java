package com.example.QLPT.Funsion.User.Pay;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

public class PayDTO {

    // DTO hứng dữ liệu từ SePay Webhook bắn về
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    // Thêm @JsonIgnoreProperties để bỏ qua các trường SePay gửi thừa, tránh lỗi map JSON
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SePayWebhookRequest {
        private String gateway;
        private String transactionDate;
        private String accountNumber;
        private String subAccount;

        // ĐÃ SỬA CHỖ NÀY: Dùng @JsonAlias để bắt các trường hợp tên biến SePay trả về
        @JsonAlias({"amountIn", "transferAmount"})
        private BigDecimal amountIn;

        private BigDecimal amountOut;
        private BigDecimal accumulated;
        private String code;

        // ĐÃ SỬA CHỖ NÀY: Dùng @JsonAlias để bắt nội dung chuyển khoản
        @JsonAlias({"transactionContent", "content"})
        private String transactionContent;

        private String referenceCode;
        private String description;
    }

    // DTO trả về cho App/Web để hiển thị mã VietQR
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VietQRResponse {
        private Long invoiceId;
        private String qrUrl;
        private String bankId;
        private String accountNo;
        private String accountName;
        private BigDecimal amount;
        private String transferContent;
    }
}