package com.example.QLPT.Funsion.landlady.Property;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/landlord/properties")
public class LandlordPropertyController {

    @Autowired
    private LandlordPropertyService landlordPropertyService;

    // Lấy toàn bộ danh sách Khu trọ -> Phòng -> Khách thuê
    @GetMapping("/dashboard")
    public ResponseEntity<List<LandlordPropertyDTO>> getLandlordDashboard() {
        return ResponseEntity.ok(landlordPropertyService.getDashboardData());
    }

    // Sửa thông tin phòng
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<LandlordRoomDTO> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomRequestDTO request) {
        return ResponseEntity.ok(landlordPropertyService.updateRoom(roomId, request));
    }

    // Xóa phòng
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long roomId) {
        landlordPropertyService.deleteRoom(roomId);
        return ResponseEntity.ok("Xóa phòng thành công!");
    }

    // Thêm người vào phòng (Ký hợp đồng)
    @PostMapping("/rooms/{roomId}/assign-tenant")
    public ResponseEntity<String> assignTenant(
            @PathVariable Long roomId,
            @Valid @RequestBody AssignTenantRequestDTO request) {
        landlordPropertyService.assignTenantToRoom(roomId, request);
        return ResponseEntity.ok("Thêm người thuê vào phòng thành công!");
    }

    // Đuổi/Xóa người đang ở (Trả phòng)
    @PostMapping("/rooms/{roomId}/remove-tenant")
    public ResponseEntity<String> removeTenant(@PathVariable Long roomId) {
        landlordPropertyService.removeTenantFromRoom(roomId);
        return ResponseEntity.ok("Đã trả phòng. Phòng hiện đang trống!");
    }
}