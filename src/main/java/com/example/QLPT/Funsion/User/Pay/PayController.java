package com.example.QLPT.Funsion.User.Pay;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    // 1. API cho Client gọi để lấy mã VietQR hiển thị cho khách quét
    @GetMapping("/vietqr/{invoiceId}")
    public ResponseEntity<?> getVietQR(@PathVariable Long invoiceId) {
        try {
            return ResponseEntity.ok(payService.generateVietQR(invoiceId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }

    // 2. API Webhook - Cấu hình URL này lên hệ thống SePay (VD: https://domain-cua-ban.com/api/pay/sepay-webhook)
    @PostMapping("/sepay-webhook")
    public ResponseEntity<Map<String, Boolean>> handleSePayWebhook(
            @Valid @RequestBody PayDTO.SePayWebhookRequest request) {

        // Gọi Service xử lý nghiệp vụ cộng tiền
        payService.processSePayWebhook(request);

        // SePay yêu cầu trả về HTTP Status 200 OK để xác nhận đã nhận được Webhook
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}