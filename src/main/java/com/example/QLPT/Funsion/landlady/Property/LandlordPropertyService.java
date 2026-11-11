package com.example.QLPT.Funsion.landlady.Property;

import com.example.QLPT.Enity.Contract;
import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Property;
import com.example.QLPT.Enity.Room;
import com.example.QLPT.Enity.User;
import com.example.QLPT.Repository.ContractRepository;
import com.example.QLPT.Repository.InvoiceRepository;
import com.example.QLPT.Repository.PropertyRepository;
import com.example.QLPT.Repository.RoomRepository;
import com.example.QLPT.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LandlordPropertyService {

    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired private InvoiceRepository invoiceRepository;

    // =========================================================
    // 1. LẤY DỮ LIỆU HIỂN THỊ (GIỮ NGUYÊN CODE CŨ CỦA BẠN)
    // =========================================================
    public List<LandlordPropertyDTO> getDashboardData() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User landlord = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không xác định được danh tính chủ trọ!"));

        List<Property> properties = propertyRepository.findByLandlordIdAndDeletedAtIsNull(landlord.getId());

        return properties.stream().map(property -> {
            List<Room> rooms = roomRepository.findByPropertyId(property.getId());

            List<LandlordRoomDTO> roomDTOs = rooms.stream().map(room -> {
                LandlordTenantDTO tenantDTO = null;

                if (room.getStatus() == Enum.RoomStatus.RENTED) {
                    Optional<Contract> activeContract = contractRepository.findByRoomIdAndStatus(room.getId(), Enum.ContractStatus.ACTIVE);
                    if (activeContract.isPresent()) {
                        User tenant = activeContract.get().getTenant();
                        tenantDTO = LandlordTenantDTO.builder()
                                .id(tenant.getId())
                                .fullName(tenant.getFullName())
                                .phoneNumber(tenant.getPhoneNumber())
                                .email(tenant.getEmail())
                                .build();
                    }
                }
                return LandlordRoomDTO.builder()
                        .id(room.getId())
                        .roomNumber(room.getRoomNumber())
                        .basePrice(room.getBasePrice())
                        .status(room.getStatus() != null ? room.getStatus().name() : null)
                        .currentTenant(tenantDTO) // NẾU NULL -> PHÒNG TRỐNG, NẾU CÓ DATA -> PHÒNG CÓ NGƯỜI
                        .build();
            }).collect(Collectors.toList());

            return LandlordPropertyDTO.builder()
                    .id(property.getId())
                    .name(property.getName())
                    .address(property.getAddress())
                    .rooms(roomDTOs)
                    .build();
        }).collect(Collectors.toList());
    }

    // =========================================================
    // 2. NGHIỆP VỤ QUẢN LÝ PHÒNG VÀ NGƯỜI THUÊ
    // =========================================================

    // Sửa thông tin phòng
    @Transactional
    public LandlordRoomDTO updateRoom(Long roomId, RoomRequestDTO request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        room.setRoomNumber(request.getRoomNumber());
        room.setBasePrice(request.getBasePrice());
        room.setUpdatedAt(LocalDateTime.now());

        Room savedRoom = roomRepository.save(room);

        return LandlordRoomDTO.builder()
                .id(savedRoom.getId())
                .roomNumber(savedRoom.getRoomNumber())
                .basePrice(savedRoom.getBasePrice())
                .status(savedRoom.getStatus().name())
                .build();
    }

    // Xóa phòng
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        if (room.getStatus() == Enum.RoomStatus.RENTED) {
            throw new RuntimeException("Phòng đang có người thuê, không thể xóa! Hãy cho khách trả phòng trước.");
        }
        roomRepository.delete(room);
    }

    // Thêm người thuê vào phòng trống (Tạo hợp đồng)
    @Transactional
    public void assignTenantToRoom(Long roomId, AssignTenantRequestDTO request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        if (room.getStatus() != Enum.RoomStatus.AVAILABLE) {
            throw new IllegalArgumentException("Phòng hiện không trống!");
        }

        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Người thuê không tồn tại!"));

        // 1. Chuyển phòng thành ĐÃ THUÊ
        if (tenant.getRole() != Enum.Role.TENANT) {
            throw new IllegalArgumentException("Tài khoản phải là khách thuê!");
        }
        room.setStatus(Enum.RoomStatus.RENTED);
        roomRepository.save(room);

        // 2. Ký hợp đồng mới
        Contract contract = new Contract();
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setDepositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : java.math.BigDecimal.ZERO);
        contract.setDepositStatus(Enum.DepositStatus.UNPAID);
        contract.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        contract.setStatus(Enum.ContractStatus.ACTIVE);
        contract.setCreatedAt(LocalDateTime.now());
        contractRepository.save(contract);
    }

    // Xóa/Đuổi người thuê khỏi phòng (Trả phòng)
    @Transactional
    public void removeTenantFromRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        if (room.getStatus() != Enum.RoomStatus.RENTED) {
            throw new RuntimeException("Phòng đang trống, không có người để xóa!");
        }

        // Tìm hợp đồng đang hoạt động của phòng này
        Contract activeContract = contractRepository.findByRoomIdAndStatus(room.getId(), Enum.ContractStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Lỗi dữ liệu: Phòng báo có người nhưng không tìm thấy hợp đồng ACTIVE!"));

        // 1. Chấm dứt hợp đồng (Không xóa User khỏi CSDL, chỉ kết thúc hợp đồng thuê)
        if (invoiceRepository.findByContractId(activeContract.getId()).stream()
                .anyMatch(i -> i.getDeletedAt() == null && i.getStatus() != Enum.InvoiceStatus.PAID)) {
            throw new IllegalArgumentException("Cần thanh toán hết công nợ trước khi trả phòng!");
        }
        activeContract.setStatus(Enum.ContractStatus.TERMINATED);
        activeContract.setActualEndDate(LocalDate.now());
        contractRepository.save(activeContract);

        // 2. Chuyển phòng về trạng thái TRỐNG
        room.setStatus(Enum.RoomStatus.AVAILABLE);
        roomRepository.save(room);
    }
}
