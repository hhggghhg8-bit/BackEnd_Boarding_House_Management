package com.example.QLPT.Funsion.landlady.ServiceRoom;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/landlord/room-services")
@RequiredArgsConstructor
public class ServiceRoomController {

    private final ServiceRoomService serviceRoomService;

    @GetMapping("/catalog")
    public ResponseEntity<List<ServiceRoomDTO.CatalogResponse>> getCatalog() {
        return ResponseEntity.ok(serviceRoomService.getAllAvailableServices());
    }

    @PostMapping("/catalog")
    public ResponseEntity<String> createCatalogService(@Valid @RequestBody ServiceRoomDTO.CreateCatalogRequest request) {
        serviceRoomService.createCatalogService(request);
        return ResponseEntity.ok("Đã thêm dịch vụ gốc vào hệ thống!");
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ServiceRoomDTO.RoomBillingSummary> getRoomBillingSummary(@PathVariable Long roomId) {
        return ResponseEntity.ok(serviceRoomService.getRoomBillingSummary(roomId));
    }

    @PostMapping("/room/{roomId}")
    public ResponseEntity<String> assignService(@PathVariable Long roomId, @Valid @RequestBody ServiceRoomDTO.AssignRequest request) {
        try {
            serviceRoomService.assignServiceToRoom(roomId, request);
            return ResponseEntity.ok("Thêm dịch vụ cho phòng thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }

    @PutMapping("/{roomServiceId}/price")
    public ResponseEntity<String> updateCustomPrice(@PathVariable Long roomServiceId, @RequestParam(required = false) BigDecimal customPrice) {
        try {
            serviceRoomService.updateCustomPrice(roomServiceId, customPrice);
            return ResponseEntity.ok("Cập nhật giá dịch vụ thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }

    @DeleteMapping("/{roomServiceId}")
    public ResponseEntity<String> removeService(@PathVariable Long roomServiceId) {
        try {
            serviceRoomService.removeServiceFromRoom(roomServiceId);
            return ResponseEntity.ok("Đã hủy dịch vụ của phòng này!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }

    @PostMapping(value = "/room/{roomId}/record-utility", consumes = {"multipart/form-data"})
    public ResponseEntity<?> recordUtility(
            @PathVariable Long roomId,
            @Valid @RequestPart("data") ServiceRoomDTO.RecordUtilityRequest request,
            @RequestPart("image") MultipartFile meterImage) {
        try {
            ServiceRoomDTO.UtilityReadingResponse result = serviceRoomService.recordUtility(roomId, request, meterImage);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
    // API Lấy lịch sử hóa đơn
    @GetMapping("/room/{roomId}/history")
    public ResponseEntity<List<ServiceRoomDTO.InvoiceHistoryResponse>> getInvoiceHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(serviceRoomService.getInvoiceHistory(roomId));
    }

    // API Xác nhận thu tiền mặt
    @PutMapping("/invoice/{invoiceId}/pay-cash")
    public ResponseEntity<String> confirmCashPayment(@PathVariable Long invoiceId) {
        try {
            serviceRoomService.confirmCashPayment(invoiceId);
            return ResponseEntity.ok("Xác nhận thu tiền mặt thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.example.QLPT.Config.ApiErrors.message(e));
        }
    }
}