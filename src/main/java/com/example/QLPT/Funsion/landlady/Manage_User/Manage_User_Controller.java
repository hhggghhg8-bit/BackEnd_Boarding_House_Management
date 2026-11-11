package com.example.QLPT.Funsion.landlady.Manage_User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/landlady/users") // Base URL cho module này
@RequiredArgsConstructor
public class Manage_User_Controller {

    private final Manage_User_Service manageUserService;

    @GetMapping
    public ResponseEntity<List<Manage_User_DTO>> getAllUsers() {
        return ResponseEntity.ok(manageUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Manage_User_DTO> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(manageUserService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<Manage_User_DTO> createUser(@Valid @RequestBody Manage_User_DTO dto) {
        try {
            Manage_User_DTO createdUser = manageUserService.createUser(dto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Manage_User_DTO> updateUser(@PathVariable Long id, @Valid @RequestBody Manage_User_DTO dto) {
        try {
            Manage_User_DTO updatedUser = manageUserService.updateUser(id, dto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // --- MỚI: API LẤY LỊCH SỬ THUÊ NHÀ (THAY THẾ DELETE) ---
    @GetMapping("/{id}/history")
    public ResponseEntity<List<Manage_User_DTO.TenantHistory>> getUserHistory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(manageUserService.getUserHistory(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}