package com.example.QLPT.Funsion.Login.Login;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String password;

    // FCM tokens are stored as TEXT; they can exceed 255 characters.
    private String fcmToken;
}
