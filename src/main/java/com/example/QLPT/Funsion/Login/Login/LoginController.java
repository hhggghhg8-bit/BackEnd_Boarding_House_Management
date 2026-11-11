package com.example.QLPT.Funsion.Login.Login;

import jakarta.validation.Valid;
import com.example.QLPT.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth") // Prefix API
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            // Gọi service xử lý đăng nhập
            LoginResponse response = loginService.authenticateUser(loginDTO);

            // Trả về HTTP 200 OK kèm theo thông tin Token
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Trả về HTTP 400 hoặc 401 nếu sai email/mật khẩu
            return ResponseEntity.badRequest().body("Sai email hoặc mật khẩu! Vui lòng thử lại.");
        }
    }

    // THÊM MỚI: API xóa FCM Token khi người dùng ấn Đăng Xuất trên App
    @PostMapping("/logout-fcm")
    public ResponseEntity<?> logoutFcm(Principal principal) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                user.setFcmToken(null); // Gỡ Token khỏi thiết bị
                userRepository.save(user);
            });
        }
        return ResponseEntity.ok("Đã gỡ FCM Token an toàn!");
    }
}