package com.example.QLPT.Funsion.Login.UpdateInfor;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant") // Chặn quyền, bắt buộc phải có Token
public class UpdateInforController {

    @Autowired
    private UpdateInforService updateInforService;

    @PutMapping("/update-profile") // Dùng phương thức PUT cho việc cập nhật
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateInforDTO updateInforDTO) {
        try {
            String message = updateInforService.updateInformation(updateInforDTO);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
}