package com.example.QLPT.Funsion.User.prf;

import lombok.*;

public class ProfileDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserProfileResponse {
        private Long id;
        private String email;
        private String fullName;
        private String phoneNumber;
        private String identityNumber;
        private String avatarUrl;
        private String role;
        private String status;
    }
}