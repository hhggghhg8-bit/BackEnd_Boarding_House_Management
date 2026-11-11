package com.example.QLPT.Funsion.User.total;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. Xem thông tin phòng đang thuê
    @GetMapping("/my-room")
    public ResponseEntity<?> getMyRoomInfo() {
        try {
            return ResponseEntity.ok(userService.getMyRoom());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }

    // 2. Xem danh sách lịch sử hóa đơn (Tiền phòng, điện nước...)
    @GetMapping("/my-invoices")
    public ResponseEntity<List<UserDTO.MyInvoiceResponse>> getMyInvoices() {
        return ResponseEntity.ok(userService.getMyInvoices());
    }

    // THÊM API NÀY ĐỂ APP FLUTTER GỌI LÊN LƯU TOKEN
    // 3. API cập nhật FCM Token cho thông báo Push Notification
    @PutMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(@Valid @RequestBody UserDTO.FcmTokenRequest request) {
        try {
            userService.updateFcmToken(request.getFcmToken());
            return ResponseEntity.ok("Cập nhật FCM Token thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
}