package com.example.QLPT.Funsion.Login.UpdateInfor;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateInforDTO {
    // Các trường cập nhật cho bảng User
    @Size(max = 255)
    private String fullName;
    @Size(max = 255)
    private String phoneNumber;
    @Size(max = 255)
    private String identityNumber;
    @Size(max = 255)
    private String avatarUrl;

    // Các trường cập nhật bổ sung cho bảng TenantProfile
    private LocalDate dateOfBirth; // Định dạng gửi lên: "YYYY-MM-DD" (VD: "2004-10-11")
    @Size(max = 255)
    private String hometown;
    @Size(max = 255)
    private String occupation;
}