package com.example.QLPT.Funsion.Login.Login;

import com.example.QLPT.Enity.User;
import com.example.QLPT.Repository.UserRepository;
import com.example.QLPT.Security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    public LoginResponse authenticateUser(LoginDTO loginDTO) {
        // 1. Kiểm tra tài khoản và mật khẩu thông qua Spring Security
        // Nếu sai mật khẩu hoặc không có user, nó sẽ tự động văng Exception ở dòng này
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        // 2. Nếu thành công, set thông tin đăng nhập vào Security Context của hệ thống
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Gọi hàm tạo JWT Token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 4. Truy xuất thêm thông tin User từ Database để trả về cho Front-end
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // ---> 5. CẬP NHẬT FCM TOKEN VÀO DATABASE MỖI KHI LOGIN <---
        if (loginDTO.getFcmToken() != null && !loginDTO.getFcmToken().trim().isEmpty()) {
            user.setFcmToken(loginDTO.getFcmToken());
            userRepository.save(user); // Lưu lại token mới vào DB
        }

        // 6. Trả về đối tượng LoginResponse
        return new LoginResponse(
                jwt,
                "Bearer",
                user.getEmail(),
                user.getRole().name(),
                user.getFullName()
        );
    }
}