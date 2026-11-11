package com.example.QLPT.Funsion.Login.Register;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Prefix API
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            // Gọi Service để xử lý đăng ký
            String message = registerService.registerUser(registerDTO);

            // Trả về HTTP 200 OK nếu thành công
            return ResponseEntity.ok().body(message);

        } catch (RuntimeException e) {
            // Trả về HTTP 400 Bad Request nếu lỗi (ví dụ: trùng email)
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
}