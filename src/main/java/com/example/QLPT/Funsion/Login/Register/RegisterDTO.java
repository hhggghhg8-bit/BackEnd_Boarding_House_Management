package com.example.QLPT.Funsion.Login.Register;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank @Email @Size(max = 255)
    private String email;
    @NotBlank @Size(max = 72)
    private String password;
    @Size(max = 255)
    private String fullName;
    @Size(max = 255)
    private String phoneNumber;
    @Size(max = 255)
    private String identityNumber;
}