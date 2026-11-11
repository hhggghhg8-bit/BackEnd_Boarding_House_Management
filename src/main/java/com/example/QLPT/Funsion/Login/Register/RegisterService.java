package com.example.QLPT.Funsion.Login.Register;

import com.example.QLPT.Enity.User;
import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegisterService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(RegisterDTO registerDTO) {
        // 1. Kiểm tra email đã tồn tại trong Database chưa
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email này đã được đăng ký trong hệ thống!");
        }

        // 2. Chuyển đổi dữ liệu từ DTO sang Entity User
        User newUser = new User();
        newUser.setEmail(registerDTO.getEmail());

        // Bắt buộc phải mã hóa mật khẩu trước khi lưu vào DB
        newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        newUser.setFullName(registerDTO.getFullName());
        newUser.setPhoneNumber(registerDTO.getPhoneNumber());
        newUser.setIdentityNumber(registerDTO.getIdentityNumber());

        // 3. Gán quyền thấp nhất và trạng thái mặc định
        newUser.setRole(Enum.Role.TENANT); // Cấp quyền người thuê trọ
        newUser.setStatus(Enum.UserStatus.ACTIVE); // Kích hoạt tài khoản ngay

        // 4. Ghi nhận thời gian
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        // 5. Lưu vào Database
        userRepository.save(newUser);

        return "Đăng ký tài khoản thành công!";
    }
}