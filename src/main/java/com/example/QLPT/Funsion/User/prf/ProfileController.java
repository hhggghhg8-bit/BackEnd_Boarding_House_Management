package com.example.QLPT.Funsion.User.prf;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // API: GET /api/profile/me
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        try {
            return ResponseEntity.ok(profileService.getMyProfile());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
}