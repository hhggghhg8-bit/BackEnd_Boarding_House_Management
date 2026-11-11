package com.example.QLPT.Funsion.User.prf;

import com.example.QLPT.Enity.User;
import com.example.QLPT.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileDTO.UserProfileResponse getMyProfile() {
        // Lấy email của người dùng đang đăng nhập từ Token (SecurityContext)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tìm User trong Database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản!"));

        // Map dữ liệu từ Entity sang DTO để trả về Frontend
        return ProfileDTO.UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .identityNumber(user.getIdentityNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .build();
    }
}