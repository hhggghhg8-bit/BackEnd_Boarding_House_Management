package com.example.QLPT.Funsion.landlady.AddBoardingHouse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/landlord/manage")
public class LandlordManageController {

    @Autowired
    private LandlordManageService landlordManageService;

    // 1. API Thêm Khu trọ
    @PostMapping("/properties")
    public ResponseEntity<?> createProperty(@Valid @RequestBody LandlordManageDTOs.CreatePropertyDTO dto) {
        try {
            return ResponseEntity.ok(landlordManageService.createProperty(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }

    // 2. API Thêm Phòng trọ
    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@Valid @RequestBody LandlordManageDTOs.CreateRoomDTO dto) {
        try {
            return ResponseEntity.ok(landlordManageService.createRoom(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }

    // 3. API Thêm Người thuê vào phòng
    @PostMapping("/assign-tenant")
    public ResponseEntity<?> assignTenant(@Valid @RequestBody LandlordManageDTOs.AssignTenantDTO dto) {
        try {
            return ResponseEntity.ok(landlordManageService.assignTenantToRoom(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
    // 4. API Thêm Nhiều Phòng Tự Động
    @PostMapping("/rooms/bulk")
    public ResponseEntity<?> createMultipleRooms(@Valid @RequestBody LandlordManageDTOs.CreateMultipleRoomsDTO dto) {
        try {
            return ResponseEntity.ok(landlordManageService.createMultipleRooms(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
    // 5. API Sửa thông tin Phòng trọ
    @PutMapping("/rooms/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @Valid @RequestBody LandlordManageDTOs.UpdateRoomDTO dto) {
        try {
            return ResponseEntity.ok(landlordManageService.updateRoom(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }

    // 6. API Trả phòng (Thanh lý hợp đồng)
    @PutMapping("/rooms/{roomId}/checkout")
    public ResponseEntity<?> checkoutTenant(@PathVariable Long roomId) {
        try {
            return ResponseEntity.ok(landlordManageService.checkoutTenant(roomId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
}