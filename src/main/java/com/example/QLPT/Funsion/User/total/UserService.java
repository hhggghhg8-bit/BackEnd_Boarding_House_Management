package com.example.QLPT.Funsion.User.total;

import com.example.QLPT.Enity.Contract;
import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Invoice;
import com.example.QLPT.Enity.Room;
import com.example.QLPT.Enity.RoomService;
import com.example.QLPT.Enity.User;
import com.example.QLPT.Enity.UtilityReading;
import com.example.QLPT.Repository.ContractRepository;
import com.example.QLPT.Repository.InvoiceRepository;
import com.example.QLPT.Repository.RoomServiceRepository;
import com.example.QLPT.Repository.UserRepository;
import com.example.QLPT.Repository.UtilityReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final UtilityReadingRepository utilityReadingRepository;
    private final RoomServiceRepository roomServiceRepository;

    // Lấy User đang đăng nhập từ Security Context (JWT)
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng!"));
    }

    // Lấy phòng người dùng đang thuê (Dựa vào hợp đồng đang Active)
    @Transactional(readOnly = true)
    public UserDTO.MyRoomResponse getMyRoom() {
        User tenant = getCurrentUser();

        // Ép List thành Optional và ném lỗi nếu không có
        Contract activeContract = contractRepository.findByTenantIdAndStatus(tenant.getId(), Enum.ContractStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Bạn hiện tại không có hợp đồng thuê phòng nào đang hoạt động!"));

        Room room = activeContract.getRoom();

        return UserDTO.MyRoomResponse.builder()
                .roomId(room.getId())
                .propertyName(room.getProperty().getName())
                .roomNumber(room.getRoomNumber())
                .propertyAddress(room.getProperty().getAddress())
                .basePrice(room.getBasePrice())
                .build();
    }

    // Lấy danh sách hóa đơn (lịch sử và tháng hiện tại)
    @Transactional(readOnly = true)
    public List<UserDTO.MyInvoiceResponse> getMyInvoices() {
        User tenant = getCurrentUser();

        // Tìm hợp đồng của User (Lấy cái đầu tiên)
        Optional<Contract> activeContractOpt = contractRepository.findByTenantIdAndStatus(tenant.getId(), Enum.ContractStatus.ACTIVE)
                .stream().findFirst();

        // Nếu không có hợp đồng thì trả về list rỗng thay vì báo lỗi crash app
        if (activeContractOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Contract contract = activeContractOpt.get();
        Long roomId = contract.getRoom().getId();

        // Lấy tất cả hóa đơn thuộc hợp đồng này
        List<Invoice> invoices = invoiceRepository.findByContractId(contract.getId());

        // Lấy danh sách các dịch vụ kèm theo phòng để tính giá (Wifi, Rác...)
        List<RoomService> roomServices = roomServiceRepository.findByRoomId(roomId);

        return invoices.stream().map(invoice -> {
                    // ĐÃ SỬA: Sử dụng trực tiếp hàm findByRoomIdAndBillingCycle thay vì findAll().stream()
                    Optional<UtilityReading> readingOpt = utilityReadingRepository.findByRoomIdAndBillingCycle(roomId, invoice.getBillingCycle());

                    Integer oldE = 0, newE = 0, usageE = 0;
                    Integer oldW = 0, newW = 0, usageW = 0;
                    BigDecimal totalE = BigDecimal.ZERO;
                    BigDecimal totalW = BigDecimal.ZERO;
                    String imgUrl = null;

                    if (readingOpt.isPresent()) {
                        UtilityReading r = readingOpt.get();
                        oldE = r.getOldElectricity();
                        newE = r.getNewElectricity();
                        oldW = r.getOldWater();
                        newW = r.getNewWater();

                        usageE = Boolean.TRUE.equals(r.getIsMeterReset()) ? newE : (newE - oldE);
                        usageW = Boolean.TRUE.equals(r.getIsMeterReset()) ? newW : (newW - oldW);
                        imgUrl = r.getMeterImageUrl();

                        // Lấy đơn giá điện/nước để tính tiền
                        BigDecimal ePrice = getServicePrice(roomServices, "Điện");
                        BigDecimal wPrice = getServicePrice(roomServices, "Nước");

                        totalE = ePrice.multiply(BigDecimal.valueOf(usageE));
                        totalW = wPrice.multiply(BigDecimal.valueOf(usageW));
                    }

                    // Lấy các dịch vụ ngoài Điện, Nước, Phòng (Ví dụ: Wifi, Vệ sinh)
                    List<UserDTO.ServiceDetail> extraServices = roomServices.stream()
                            .filter(rs -> !rs.getService().getName().toLowerCase().contains("điện") &&
                                    !rs.getService().getName().toLowerCase().contains("nước") &&
                                    !rs.getService().getName().toLowerCase().contains("phòng"))
                            .map(rs -> UserDTO.ServiceDetail.builder()
                                    .serviceName(rs.getService().getName())
                                    .appliedPrice(rs.getCustomPrice() != null ? rs.getCustomPrice() : rs.getService().getDefaultPrice())
                                    .build())
                            .collect(Collectors.toList());

                    return UserDTO.MyInvoiceResponse.builder()
                            .invoiceId(invoice.getId())
                            .billingCycle(invoice.getBillingCycle())
                            .dueDate(invoice.getDueDate())
                            .totalAmount(invoice.getTotalAmount())
                            .status(invoice.getStatus().name())
                            .meterImageUrl(imgUrl)
                            .oldElectricity(oldE)
                            .newElectricity(newE)
                            .electricUsage(usageE)
                            .electricTotal(totalE)
                            .oldWater(oldW)
                            .newWater(newW)
                            .waterUsage(usageW)
                            .waterTotal(totalW)
                            .extraServices(extraServices)
                            .build();

                }).sorted((i1, i2) -> i2.getBillingCycle().compareTo(i1.getBillingCycle())) // Xếp hóa đơn mới nhất lên đầu
                .collect(Collectors.toList());
    }

    // Hàm phụ để lấy giá dịch vụ
    private BigDecimal getServicePrice(List<RoomService> services, String serviceName) {
        return services.stream()
                .filter(s -> s.getService().getName().equalsIgnoreCase(serviceName))
                .findFirst()
                .map(s -> s.getCustomPrice() != null ? s.getCustomPrice() : s.getService().getDefaultPrice())
                .orElse(BigDecimal.ZERO);
    }

    // THÊM HÀM NÀY VÀO CUỐI CÙNG
    @Transactional
    public void updateFcmToken(String fcmToken) {
        User user = getCurrentUser(); // Lấy user đang đăng nhập qua JWT
        user.setFcmToken(fcmToken);   // Gán token thiết bị
        userRepository.save(user);    // Lưu vào database
    }
}