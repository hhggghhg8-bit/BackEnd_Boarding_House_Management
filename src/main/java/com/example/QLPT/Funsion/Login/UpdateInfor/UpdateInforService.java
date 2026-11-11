package com.example.QLPT.Funsion.Login.UpdateInfor;

import com.example.QLPT.Enity.TenantProfile;
import com.example.QLPT.Enity.User;
import com.example.QLPT.Repository.TenantProfileRepository;
import com.example.QLPT.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateInforService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantProfileRepository tenantProfileRepository;

    @Transactional
    public String updateInformation(UpdateInforDTO dto) {
        // 1. Lấy email của người dùng đang gọi API từ JWT Token
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm User trong CSDL
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng đang đăng nhập!"));

        // 3. Cập nhật thông tin bảng User (Kiểm tra null để không làm mất data cũ nếu gửi thiếu)
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getIdentityNumber() != null) user.setIdentityNumber(dto.getIdentityNumber());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. Tìm hoặc tạo mới TenantProfile
        TenantProfile profile = tenantProfileRepository.findByUserId(user.getId())
                .orElse(new TenantProfile());

        if (profile.getId() == null) {
            // Nếu chưa có profile (lần đầu cập nhật) thì gắn user vào và set thời gian tạo
            profile.setUser(user);
            profile.setCreatedAt(LocalDateTime.now());
        }

        // 5. Cập nhật thông tin bảng TenantProfile
        if (dto.getDateOfBirth() != null) profile.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getHometown() != null) profile.setHometown(dto.getHometown());
        if (dto.getOccupation() != null) profile.setOccupation(dto.getOccupation());

        profile.setUpdatedAt(LocalDateTime.now());
        tenantProfileRepository.save(profile);

        return "Cập nhật thông tin hồ sơ thành công!";
    }
}