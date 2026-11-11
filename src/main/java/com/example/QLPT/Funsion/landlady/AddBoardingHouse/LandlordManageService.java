package com.example.QLPT.Funsion.landlady.AddBoardingHouse;

import com.example.QLPT.Enity.Contract;
import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Property;
import com.example.QLPT.Enity.Room;
import com.example.QLPT.Enity.User;
import com.example.QLPT.Repository.ContractRepository;
import com.example.QLPT.Repository.InvoiceRepository; // Import InvoiceRepository
import com.example.QLPT.Repository.PropertyRepository;
import com.example.QLPT.Repository.RoomRepository;
import com.example.QLPT.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Service
public class LandlordManageService {

    @Autowired private UserRepository userRepository;
    @Autowired private PropertyRepository propertyRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private InvoiceRepository invoiceRepository; // Đưa lên đây để dùng chung cho cả class

    // Lấy thông tin chủ trọ đang đăng nhập (dùng chung cho các hàm)
    private User getCurrentLandlord() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Chủ trọ!"));
    }

    // =====================================
    // 1. THÊM KHU TRỌ MỚI
    // =====================================
    public String createProperty(LandlordManageDTOs.CreatePropertyDTO dto) {
        User landlord = getCurrentLandlord();

        Property property = Property.builder()
                .landlord(landlord)
                .name(dto.getName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .rules(dto.getRules())
                .billingDay(dto.getBillingDay())
                .paymentDeadlineDay(dto.getPaymentDeadlineDay())
                .createdAt(LocalDateTime.now())
                .build();

        propertyRepository.save(property);
        return "Thêm khu trọ thành công!";
    }

    // =====================================
    // 2. THÊM PHÒNG TRỌ MỚI
    // =====================================
    public String createRoom(LandlordManageDTOs.CreateRoomDTO dto) {
        User landlord = getCurrentLandlord();

        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu trọ!"));

        if (!property.getLandlord().getId().equals(landlord.getId())) {
            throw new RuntimeException("Bạn không có quyền thêm phòng vào khu trọ của người khác!");
        }

        if (roomRepository.existsByPropertyIdAndRoomNumber(dto.getPropertyId(), dto.getRoomNumber())) {
            throw new RuntimeException("Lỗi: Số phòng [" + dto.getRoomNumber() + "] đã tồn tại trong khu trọ này!");
        }

        Room room = Room.builder()
                .property(property)
                .roomNumber(dto.getRoomNumber())
                .area(dto.getArea())
                .maxCapacity(dto.getMaxCapacity())
                .basePrice(dto.getBasePrice())
                .amenities(dto.getAmenities())
                .status(Enum.RoomStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();

        roomRepository.save(room);
        return "Thêm phòng trọ [" + dto.getRoomNumber() + "] thành công!";
    }

    // =====================================
    // 3. THÊM NGƯỜI THUÊ (TẠO HỢP ĐỒNG)
    // =====================================
    @Transactional
    public String assignTenantToRoom(LandlordManageDTOs.AssignTenantDTO dto) {
        User landlord = getCurrentLandlord();

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        if (!room.getProperty().getLandlord().getId().equals(landlord.getId())) {
            throw new RuntimeException("Bạn không có quyền quản lý phòng này!");
        }
        if (room.getStatus() != Enum.RoomStatus.AVAILABLE) {
            throw new RuntimeException("Phòng này hiện không trống!");
        }

        User tenant = userRepository.findByEmail(dto.getTenantEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách thuê có email này!"));

        if (tenant.getRole() != Enum.Role.TENANT) {
            throw new RuntimeException("Email này không phải là tài khoản Khách thuê!");
        }

        Contract contract = Contract.builder()
                .room(room)
                .tenant(tenant)
                .startDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now())
                .endDate(dto.getEndDate())
                .depositAmount(dto.getDeposit() != null ? dto.getDeposit() : BigDecimal.ZERO)
                .depositStatus(Enum.DepositStatus.UNPAID)
                .status(Enum.ContractStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        contractRepository.save(contract);

        room.setStatus(Enum.RoomStatus.RENTED);
        roomRepository.save(room);

        return "Đã thêm khách thuê " + tenant.getFullName() + " vào phòng " + room.getRoomNumber() + " thành công!";
    }

    // =====================================
    // 4. THÊM NHIỀU PHÒNG TRỌ TỰ ĐỘNG
    // =====================================
    @Transactional
    public String createMultipleRooms(LandlordManageDTOs.CreateMultipleRoomsDTO dto) {
        User landlord = getCurrentLandlord();

        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu trọ!"));

        if (!property.getLandlord().getId().equals(landlord.getId())) {
            throw new RuntimeException("Bạn không có quyền thêm phòng vào khu trọ của người khác!");
        }

        java.util.List<Room> roomsToSave = new java.util.ArrayList<>();

        for (int floor = 1; floor <= dto.getNumberOfFloors(); floor++) {
            for (int i = 1; i <= dto.getRoomsPerFloor(); i++) {
                String roomNumber = String.format("%d%02d", floor, i);

                if (roomRepository.existsByPropertyIdAndRoomNumber(dto.getPropertyId(), roomNumber)) {
                    throw new RuntimeException("Lỗi: Phòng [" + roomNumber + "] đã tồn tại!");
                }

                Room room = Room.builder()
                        .property(property)
                        .roomNumber(roomNumber)
                        .area(dto.getArea())
                        .maxCapacity(dto.getMaxCapacity())
                        .basePrice(dto.getBasePrice())
                        .amenities(dto.getAmenities())
                        .status(Enum.RoomStatus.AVAILABLE)
                        .createdAt(LocalDateTime.now())
                        .build();

                roomsToSave.add(room);
            }
        }

        roomRepository.saveAll(roomsToSave);
        return "Đã tạo thành công " + roomsToSave.size() + " phòng trọ tự động!";
    }

    // =====================================
    // 5. SỬA THÔNG TIN PHÒNG TRỌ
    // =====================================
    @Transactional
    public String updateRoom(Long roomId, LandlordManageDTOs.UpdateRoomDTO dto) {
        User landlord = getCurrentLandlord();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng trọ có ID này!"));

        if (!room.getProperty().getLandlord().getId().equals(landlord.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa thông tin phòng này!");
        }

        if (dto.getRoomNumber() != null && !dto.getRoomNumber().equals(room.getRoomNumber())) {
            if (roomRepository.existsByPropertyIdAndRoomNumber(room.getProperty().getId(), dto.getRoomNumber())) {
                throw new RuntimeException("Lỗi: Tên phòng [" + dto.getRoomNumber() + "] đã tồn tại!");
            }
            room.setRoomNumber(dto.getRoomNumber());
        }

        if (dto.getArea() != null) room.setArea(dto.getArea());
        if (dto.getMaxCapacity() != null) room.setMaxCapacity(dto.getMaxCapacity());
        if (dto.getBasePrice() != null) room.setBasePrice(dto.getBasePrice());
        if (dto.getAmenities() != null) room.setAmenities(dto.getAmenities());

        if (dto.getStatus() != null) {
            try {
                room.setStatus(Enum.RoomStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái phòng không hợp lệ!");
            }
        }

        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);

        return "Cập nhật thành công thông tin phòng [" + room.getRoomNumber() + "]!";
    }

    // =====================================
    // 6. TRẢ PHÒNG / THANH LÝ HỢP ĐỒNG (CÓ CHECK HÓA ĐƠN NỢ)
    // =====================================
    @Transactional
    public String checkoutTenant(Long roomId) {
        User landlord = getCurrentLandlord();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        if (!room.getProperty().getLandlord().getId().equals(landlord.getId())) {
            throw new RuntimeException("Bạn không có quyền quản lý phòng này!");
        }

        Contract activeContract = contractRepository.findByRoomIdAndStatus(roomId, Enum.ContractStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Phòng này hiện không có khách thuê (không có hợp đồng ACTIVE)!"));

        // Kiểm tra xem hợp đồng này còn hóa đơn nào chưa thanh toán (UNPAID) không
        boolean hasUnpaidInvoices = invoiceRepository.findByContractId(activeContract.getId()).stream()
                .anyMatch(invoice -> invoice.getDeletedAt() == null && invoice.getStatus() != Enum.InvoiceStatus.PAID);

        if (hasUnpaidInvoices) {
            throw new RuntimeException("LỖI: Phòng này vẫn còn HÓA ĐƠN CHƯA THANH TOÁN! Yêu cầu khách hàng thanh toán toàn bộ công nợ trước khi trả phòng.");
        }

        activeContract.setStatus(Enum.ContractStatus.TERMINATED);
        activeContract.setActualEndDate(LocalDate.now());
        activeContract.setUpdatedAt(LocalDateTime.now());
        contractRepository.save(activeContract);

        room.setStatus(Enum.RoomStatus.AVAILABLE);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);

        return "Đã thanh lý hợp đồng và trả phòng " + room.getRoomNumber() + " thành công!";
    }
}