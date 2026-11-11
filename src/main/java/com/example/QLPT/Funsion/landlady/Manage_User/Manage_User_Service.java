package com.example.QLPT.Funsion.landlady.Manage_User;

import com.example.QLPT.Enity.Contract;
import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Invoice;
import com.example.QLPT.Enity.User;
import com.example.QLPT.Repository.ContractRepository;
import com.example.QLPT.Repository.InvoiceRepository;
import com.example.QLPT.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // Thêm thư viện mã hóa
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Manage_User_Service {

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder; // Thêm PasswordEncoder

    public List<Manage_User_DTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Manage_User_DTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        return mapToDTO(user);
    }

    public Manage_User_DTO createUser(Manage_User_DTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email này đã tồn tại trong hệ thống!");
        }

        User user = mapToEntity(dto);
        // Mã hóa mật khẩu mặc định 123456
        user.setPassword(passwordEncoder.encode("123456"));
        user.setCreatedAt(LocalDateTime.now());

        if (user.getRole() == null) user.setRole(Enum.Role.TENANT);
        if (user.getStatus() == null) user.setStatus(Enum.UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    public Manage_User_DTO updateUser(Long id, Manage_User_DTO dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng để cập nhật!"));

        existingUser.setFullName(dto.getFullName());
        existingUser.setPhoneNumber(dto.getPhoneNumber());
        existingUser.setIdentityNumber(dto.getIdentityCard());
        existingUser.setUpdatedAt(LocalDateTime.now());

        if (dto.getRole() != null) {
            existingUser.setRole(Enum.Role.valueOf(dto.getRole()));
        }
        if (dto.getStatus() != null) {
            existingUser.setStatus(Enum.UserStatus.valueOf(dto.getStatus()));
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToDTO(updatedUser);
    }

    // ==========================================================
    // TÍNH NĂNG MỚI: XEM LỊCH SỬ THUÊ CỦA KHÁCH (THAY THẾ XÓA)
    // ==========================================================
    @Transactional(readOnly = true)
    public List<Manage_User_DTO.TenantHistory> getUserHistory(Long userId) {
        // Lấy tất cả hợp đồng của khách này (Bao gồm cả đang thuê và đã trả phòng)
        List<Contract> allContracts = contractRepository.findByTenantId(userId);

        return allContracts.stream().map(contract -> {
                    // Lấy tất cả hóa đơn của hợp đồng này
                    List<Invoice> invoices = invoiceRepository.findByContractId(contract.getId());

                    int totalInvoices = invoices.size();
                    int unpaidInvoices = 0;
                    BigDecimal totalPaid = BigDecimal.ZERO;
                    BigDecimal totalUnpaid = BigDecimal.ZERO;

                    for (Invoice inv : invoices) {
                        if (inv.getStatus() == Enum.InvoiceStatus.PAID) {
                            totalPaid = totalPaid.add(inv.getTotalAmount());
                        } else {
                            unpaidInvoices++;
                            totalUnpaid = totalUnpaid.add(inv.getTotalAmount());
                        }
                    }

                    return Manage_User_DTO.TenantHistory.builder()
                            .contractId(contract.getId())
                            .propertyName(contract.getRoom().getProperty().getName())
                            .roomNumber(contract.getRoom().getRoomNumber())
                            .startDate(contract.getStartDate())
                            .endDate(contract.getEndDate())
                            .contractStatus(contract.getStatus().name())
                            .depositAmount(contract.getDepositAmount())
                            .totalInvoices(totalInvoices)
                            .unpaidInvoices(unpaidInvoices)
                            .totalPaidAmount(totalPaid)
                            .totalUnpaidAmount(totalUnpaid)
                            .build();
                }).sorted((h1, h2) -> h2.getStartDate().compareTo(h1.getStartDate())) // Xếp cái mới nhất lên đầu
                .collect(Collectors.toList());
    }

    private Manage_User_DTO mapToDTO(User user) {
        List<Contract> activeContracts = contractRepository.findByTenantIdAndStatus(user.getId(), Enum.ContractStatus.ACTIVE);

        List<Manage_User_DTO.RentedRoomInfo> rentedRoomsInfo = activeContracts.stream()
                .map(contract -> {
                    String currentMonthYear = String.format("%02d-%d", LocalDateTime.now().getMonthValue(), LocalDateTime.now().getYear());
                    Optional<Invoice> invoiceOpt = invoiceRepository.findByContractIdAndBillingCycle(contract.getId(), currentMonthYear);

                    Boolean isPaid = false;
                    if (invoiceOpt.isPresent() && invoiceOpt.get().getStatus() == Enum.InvoiceStatus.PAID) {
                        isPaid = true;
                    }

                    return Manage_User_DTO.RentedRoomInfo.builder()
                            .roomId(contract.getRoom().getId())
                            .propertyName(contract.getRoom().getProperty() != null ? contract.getRoom().getProperty().getName() : "Không xác định")
                            .roomNumber(contract.getRoom().getRoomNumber())
                            .price(contract.getRoom().getBasePrice())
                            .isPaidThisMonth(isPaid)
                            .build();
                })
                .collect(Collectors.toList());

        return Manage_User_DTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .identityCard(user.getIdentityNumber())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .rentedRooms(rentedRoomsInfo)
                .build();
    }

    private User mapToEntity(Manage_User_DTO dto) {
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setIdentityNumber(dto.getIdentityCard());

        if (dto.getRole() != null && !dto.getRole().isEmpty()) {
            try {
                user.setRole(Enum.Role.valueOf(dto.getRole()));
            } catch (IllegalArgumentException e) {
                user.setRole(Enum.Role.TENANT);
            }
        }

        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            try {
                user.setStatus(Enum.UserStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                user.setStatus(Enum.UserStatus.ACTIVE);
            }
        }
        return user;
    }
}