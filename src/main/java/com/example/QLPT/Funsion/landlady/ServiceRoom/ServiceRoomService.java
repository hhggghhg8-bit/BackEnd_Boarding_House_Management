package com.example.QLPT.Funsion.landlady.ServiceRoom;

import com.example.QLPT.Config.Firebase.FcmNotificationService;
import com.example.QLPT.Enity.Contract;
import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Invoice;
import com.example.QLPT.Enity.Room;
import com.example.QLPT.Enity.RoomService;
import com.example.QLPT.Enity.Service;
import com.example.QLPT.Enity.UtilityReading;
import com.example.QLPT.Repository.ContractRepository;
import com.example.QLPT.Repository.InvoiceRepository;
import com.example.QLPT.Repository.RoomRepository;
import com.example.QLPT.Repository.RoomServiceRepository;
import com.example.QLPT.Repository.ServiceRepository;
import com.example.QLPT.Repository.UtilityReadingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceRoomService {

    private final ServiceRepository serviceRepository;
    private final RoomServiceRepository roomServiceRepository;
    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final UtilityReadingRepository utilityReadingRepository;
    private final FcmNotificationService fcmNotificationService;

    @PostConstruct
    public void initDefaultServices() {
        if (serviceRepository.count() == 0) {
            serviceRepository.save(Service.builder().name("Điện").unit("kWh").defaultPrice(new BigDecimal("3500")).build());
            serviceRepository.save(Service.builder().name("Nước").unit("Khối").defaultPrice(new BigDecimal("25000")).build());
            serviceRepository.save(Service.builder().name("Wifi").unit("Tháng").defaultPrice(new BigDecimal("100000")).build());
            serviceRepository.save(Service.builder().name("Vệ sinh").unit("Tháng").defaultPrice(new BigDecimal("20000")).build());
            serviceRepository.save(Service.builder().name("Để xe").unit("Tháng").defaultPrice(new BigDecimal("50000")).build());
            System.out.println("Đã khởi tạo tự động 5 dịch vụ: Điện, Nước, Wifi, Vệ sinh, Để xe");
        }
    }

    public List<ServiceRoomDTO.CatalogResponse> getAllAvailableServices() {
        return serviceRepository.findAll().stream().map(service ->
                ServiceRoomDTO.CatalogResponse.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .unit(service.getUnit())
                        .defaultPrice(service.getDefaultPrice())
                        .build()
        ).collect(Collectors.toList());
    }

    public void createCatalogService(ServiceRoomDTO.CreateCatalogRequest request) {
        Service newService = Service.builder()
                .name(request.getName())
                .unit(request.getUnit())
                .defaultPrice(request.getDefaultPrice())
                .createdAt(LocalDateTime.now())
                .build();
        serviceRepository.save(newService);
    }

    @Transactional
    public void ensureDefaultServicesAssigned(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        List<Service> allCatalogServices = serviceRepository.findAll();
        List<RoomService> existingRoomServices = roomServiceRepository.findByRoomId(roomId);
        List<Long> existingServiceIds = existingRoomServices.stream()
                .map(rs -> rs.getService().getId())
                .collect(Collectors.toList());

        for (Service catalogService : allCatalogServices) {
            if (!existingServiceIds.contains(catalogService.getId())) {
                RoomService newRoomService = RoomService.builder()
                        .room(room)
                        .service(catalogService)
                        .customPrice(null)
                        .build();
                roomServiceRepository.save(newRoomService);
            }
        }
    }

    @Transactional(readOnly = false)
    public ServiceRoomDTO.RoomBillingSummary getRoomBillingSummary(Long roomId) {
        ensureDefaultServicesAssigned(roomId);

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));

        // Lấy danh sách dịch vụ của phòng
        List<ServiceRoomDTO.RoomServiceResponse> services = roomServiceRepository.findByRoomId(roomId).stream()
                .map(rs -> {
                    Service s = rs.getService();
                    BigDecimal appliedPrice = (rs.getCustomPrice() != null) ? rs.getCustomPrice() : s.getDefaultPrice();
                    return ServiceRoomDTO.RoomServiceResponse.builder()
                            .roomServiceId(rs.getId())
                            .serviceId(s.getId())
                            .serviceName(s.getName())
                            .unit(s.getUnit())
                            .defaultPrice(s.getDefaultPrice())
                            .customPrice(rs.getCustomPrice())
                            .appliedPrice(appliedPrice)
                            .build();
                }).collect(Collectors.toList());

        String tenantName = "Phòng trống";
        String tenantPhone = "";
        Boolean isPaidThisMonth = false;
        BigDecimal totalAmount = BigDecimal.ZERO;

        String activeBillingCycle = String.format("%02d-%d", LocalDateTime.now().getMonthValue(), LocalDateTime.now().getYear());

        Optional<Contract> activeContract = contractRepository.findByRoomIdAndStatus(roomId, Enum.ContractStatus.ACTIVE);

        if (activeContract.isPresent()) {
            Contract contract = activeContract.get();
            tenantName = contract.getTenant().getFullName();
            tenantPhone = contract.getTenant().getPhoneNumber();

            Optional<Invoice> unpaidInvoice = invoiceRepository.findAll().stream()
                    .filter(i -> i.getContract().getId().equals(contract.getId()) && i.getStatus() == Enum.InvoiceStatus.UNPAID)
                    .findFirst();

            if (unpaidInvoice.isPresent()) {
                activeBillingCycle = unpaidInvoice.get().getBillingCycle();
                totalAmount = unpaidInvoice.get().getTotalAmount();
                isPaidThisMonth = false;
            } else {
                Optional<Invoice> currentInvoice = invoiceRepository.findByContractIdAndBillingCycle(contract.getId(), activeBillingCycle);
                if (currentInvoice.isPresent()) {
                    totalAmount = currentInvoice.get().getTotalAmount();
                    isPaidThisMonth = (currentInvoice.get().getStatus() == Enum.InvoiceStatus.PAID);
                } else {
                    // SỬA LỖI TẠI ĐÂY: NẾU CHƯA CHỐT SỐ (Chưa có hóa đơn) THÌ PHẢI TÍNH TỔNG TIỀN TẠM TÍNH
                    // Tổng tiền = Tiền phòng + Tất cả các dịch vụ cố định (Wifi, Vệ sinh...) TRỪ Điện/Nước
                    totalAmount = room.getBasePrice();
                    if(totalAmount == null) {
                        totalAmount = BigDecimal.ZERO;
                    }
                    for (ServiceRoomDTO.RoomServiceResponse srv : services) {
                        String name = srv.getServiceName().toLowerCase();
                        if (!name.contains("điện") && !name.contains("nước")) {
                            if(srv.getAppliedPrice() != null) {
                                totalAmount = totalAmount.add(srv.getAppliedPrice());
                            }
                        }
                    }
                }
            }
        }

        Integer eUsage = null; BigDecimal eTotal = null;
        Integer wUsage = null; BigDecimal wTotal = null;

        final String searchCycle = activeBillingCycle;
        Optional<UtilityReading> currentReading = utilityReadingRepository.findAll().stream()
                .filter(r -> r.getRoom().getId().equals(roomId) && r.getBillingCycle().equals(searchCycle))
                .findFirst();

        if (currentReading.isPresent()) {
            UtilityReading r = currentReading.get();
            eUsage = r.getNewElectricity() - r.getOldElectricity();
            if (Boolean.TRUE.equals(r.getIsMeterReset())) eUsage = r.getNewElectricity();
            wUsage = r.getNewWater() - r.getOldWater();
            if (Boolean.TRUE.equals(r.getIsMeterReset())) wUsage = r.getNewWater();

            BigDecimal elecPrice = services.stream().filter(s -> s.getServiceName().equalsIgnoreCase("Điện")).findFirst().map(ServiceRoomDTO.RoomServiceResponse::getAppliedPrice).orElse(BigDecimal.ZERO);
            BigDecimal waterPrice = services.stream().filter(s -> s.getServiceName().equalsIgnoreCase("Nước")).findFirst().map(ServiceRoomDTO.RoomServiceResponse::getAppliedPrice).orElse(BigDecimal.ZERO);

            eTotal = elecPrice.multiply(BigDecimal.valueOf(Math.max(0, eUsage)));
            wTotal = waterPrice.multiply(BigDecimal.valueOf(Math.max(0, wUsage)));

            // Bổ sung eTotal và wTotal vào totalAmount nếu đây là trường hợp tạm tính (chưa có Invoice)
            // (Tuy nhiên, nếu đã có UtilityReading thì thường đã có Invoice. Nếu logic tạo rời rạc, cần cộng vào).
            // Do trong mã đã lấy totalAmount từ Invoice nếu có, ta chỉ cộng nếu totalAmount chưa phản ánh giá trị này
            // Ở đây, giả định logic của bạn: nếu có UtilityReading thì hóa đơn đã được tạo và có totalAmount chuẩn.
        }

        Integer lastElec = 0;
        Integer lastWater = 0;

        Optional<UtilityReading> lastReading = utilityReadingRepository.findAll().stream()
                .filter(r -> r.getRoom().getId().equals(roomId))
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .findFirst();

        if (lastReading.isPresent()) {
            lastElec = lastReading.get().getNewElectricity();
            lastWater = lastReading.get().getNewWater();
        }

        return ServiceRoomDTO.RoomBillingSummary.builder()
                .roomId(room.getId())
                .propertyName(room.getProperty().getName())
                .roomNumber(room.getRoomNumber())
                .tenantName(tenantName)
                .tenantPhone(tenantPhone)
                .isPaidThisMonth(isPaidThisMonth)
                .billingCycle(activeBillingCycle)
                .totalAmount(totalAmount)
                .electricUsage(eUsage)
                .electricTotal(eTotal)
                .waterUsage(wUsage)
                .waterTotal(wTotal)
                .lastElectricity(lastElec)
                .lastWater(lastWater)
                .services(services)
                .build();
    }

    @Transactional
    public void assignServiceToRoom(Long roomId, ServiceRoomDTO.AssignRequest request) {
        if (roomServiceRepository.existsByRoomIdAndServiceId(roomId, request.getServiceId())) {
            throw new RuntimeException("Phòng này đã được đăng ký dịch vụ này rồi!");
        }
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng!"));
        Service service = serviceRepository.findById(request.getServiceId()).orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));

        RoomService roomService = RoomService.builder().room(room).service(service).customPrice(request.getCustomPrice()).build();
        roomServiceRepository.save(roomService);
    }

    @Transactional
    public void updateCustomPrice(Long roomServiceId, BigDecimal newCustomPrice) {
        RoomService rs = roomServiceRepository.findById(roomServiceId).orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi!"));
        rs.setCustomPrice(newCustomPrice);
        roomServiceRepository.save(rs);

        Optional<Contract> activeContract = contractRepository.findByRoomIdAndStatus(rs.getRoom().getId(), Enum.ContractStatus.ACTIVE);
        if (activeContract.isPresent() && activeContract.get().getTenant().getFcmToken() != null) {
            fcmNotificationService.sendPushNotification(
                    activeContract.get().getTenant().getFcmToken(),
                    "Cập nhật giá dịch vụ",
                    "Chủ trọ vừa cập nhật giá dịch vụ " + rs.getService().getName() + " cho phòng của bạn."
            );
        }
    }

    @Transactional
    public void removeServiceFromRoom(Long roomServiceId) {
        if (!roomServiceRepository.existsById(roomServiceId)) throw new RuntimeException("Dịch vụ không tồn tại trong phòng!");
        roomServiceRepository.deleteById(roomServiceId);
    }

    @Transactional
    public ServiceRoomDTO.UtilityReadingResponse recordUtility(Long roomId, ServiceRoomDTO.RecordUtilityRequest request, MultipartFile meterImage) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy phòng!"));

        boolean isAlreadyBilled = utilityReadingRepository.findAll().stream()
                .anyMatch(r -> r.getRoom().getId().equals(roomId) && r.getBillingCycle().equalsIgnoreCase(request.getBillingCycle()));

        if (isAlreadyBilled) {
            throw new RuntimeException("Kỳ hóa đơn [" + request.getBillingCycle() + "] của phòng này ĐÃ ĐƯỢC CHỐT SỐ! Vui lòng kiểm tra lại lịch sử hoặc đổi kỳ hóa đơn khác để tránh tạo bill trùng lặp.");
        }

        if (request.getNewRoomPrice() != null && request.getNewRoomPrice().compareTo(room.getBasePrice()) != 0) {
            room.setBasePrice(request.getNewRoomPrice());
            roomRepository.save(room);
        }

        List<RoomService> roomServices = roomServiceRepository.findByRoomId(roomId);
        BigDecimal electricPrice = updateAndGetPrice(roomServices, "Điện", request.getNewElecPrice());
        BigDecimal waterPrice = updateAndGetPrice(roomServices, "Nước", request.getNewWaterPrice());

        int electricUsage = request.getNewElectricity() - request.getOldElectricity();
        int waterUsage = request.getNewWater() - request.getOldWater();

        boolean isReset = request.getIsMeterReset() != null && request.getIsMeterReset();
        if (electricUsage < 0) {
            if (isReset) electricUsage = request.getNewElectricity();
            else throw new RuntimeException("Lỗi: Chỉ số điện mới không thể nhỏ hơn số cũ!");
        }
        if (waterUsage < 0) {
            if (isReset) waterUsage = request.getNewWater();
            else throw new RuntimeException("Lỗi: Chỉ số nước mới không thể nhỏ hơn số cũ!");
        }

        BigDecimal electricTotal = electricPrice.multiply(BigDecimal.valueOf(electricUsage));
        BigDecimal waterTotal = waterPrice.multiply(BigDecimal.valueOf(waterUsage));

        BigDecimal otherServicesTotal = BigDecimal.ZERO;
        for (RoomService rs : roomServices) {
            String sName = rs.getService().getName().toLowerCase();
            if (!sName.contains("điện") && !sName.contains("nước") && !sName.contains("phòng")) {
                BigDecimal currentPrice = rs.getCustomPrice() != null ? rs.getCustomPrice() : rs.getService().getDefaultPrice();
                otherServicesTotal = otherServicesTotal.add(currentPrice);
            }
        }

        BigDecimal grandTotal = room.getBasePrice().add(electricTotal).add(waterTotal).add(otherServicesTotal);

        String imageUrl = null;
        if (meterImage != null && !meterImage.isEmpty()) {
            try {
                String uploadDir = "uploads/meters/";
                java.io.File dir = new java.io.File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = "room_" + roomId + "_" + System.currentTimeMillis() + ".jpg";
                java.io.File serverFile = new java.io.File(dir.getAbsolutePath() + java.io.File.separator + fileName);

                meterImage.transferTo(serverFile);

                imageUrl = "http://localhost:8080/uploads/meters/" + fileName;
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi lưu trữ ảnh công tơ: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Lỗi: Bắt buộc phải tải lên ảnh công tơ minh chứng!");
        }

        UtilityReading reading = new UtilityReading();
        reading.setRoom(room);
        reading.setBillingCycle(request.getBillingCycle());
        reading.setReadingDate(request.getReadingDate() != null ? request.getReadingDate() : LocalDate.now());
        reading.setOldElectricity(request.getOldElectricity());
        reading.setNewElectricity(request.getNewElectricity());
        reading.setOldWater(request.getOldWater());
        reading.setNewWater(request.getNewWater());
        reading.setIsMeterReset(isReset);
        reading.setMeterImageUrl(imageUrl);
        reading.setCreatedAt(LocalDateTime.now());
        utilityReadingRepository.save(reading);

        Optional<Contract> activeContract = contractRepository.findByRoomIdAndStatus(roomId, Enum.ContractStatus.ACTIVE);
        if (activeContract.isPresent()) {
            Contract contract = activeContract.get();

            Invoice invoice = invoiceRepository.findByContractIdAndBillingCycle(contract.getId(), request.getBillingCycle())
                    .orElse(new Invoice());

            invoice.setContract(contract);
            invoice.setBillingCycle(request.getBillingCycle());
            invoice.setTotalAmount(grandTotal);
            LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(30);
            invoice.setDueDate(dueDate);

            if (invoice.getId() == null || invoice.getStatus() == null) {
                invoice.setStatus(Enum.InvoiceStatus.UNPAID);
                invoice.setCreatedAt(LocalDateTime.now());
                invoice.setPaymentCode(generateRandomPaymentCode());
            } else {
                invoice.setUpdatedAt(LocalDateTime.now());
            }
            invoiceRepository.save(invoice);
            if (contract.getTenant().getFcmToken() != null) {
                fcmNotificationService.sendPushNotification(
                        contract.getTenant().getFcmToken(),
                        "Hóa đơn mới",
                        "Phòng của bạn vừa có hóa đơn kỳ " + request.getBillingCycle() + ". Tổng tiền: " + grandTotal + " VNĐ."
                );
            }
        } else {
            throw new RuntimeException("Phòng chưa có người thuê (Không có hợp đồng Active) nên không thể xuất Hóa Đơn!");
        }

        return ServiceRoomDTO.UtilityReadingResponse.builder()
                .billingCycle(request.getBillingCycle())
                .electricUsage(electricUsage)
                .electricTotal(electricTotal)
                .waterUsage(waterUsage)
                .waterTotal(waterTotal)
                .grandTotal(grandTotal)
                .meterImageUrl(imageUrl)
                .build();
    }

    private BigDecimal updateAndGetPrice(List<RoomService> services, String serviceName, BigDecimal newPrice) {
        RoomService rs = services.stream()
                .filter(s -> s.getService().getName().equalsIgnoreCase(serviceName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Phòng chưa cài đặt dịch vụ: " + serviceName));

        if (newPrice != null && (rs.getCustomPrice() == null || newPrice.compareTo(rs.getCustomPrice()) != 0)) {
            rs.setCustomPrice(newPrice);
            roomServiceRepository.save(rs);
            return newPrice;
        }
        return rs.getCustomPrice() != null ? rs.getCustomPrice() : rs.getService().getDefaultPrice();
    }
    @Transactional(readOnly = true)
    public List<ServiceRoomDTO.InvoiceHistoryResponse> getInvoiceHistory(Long roomId) {
        Optional<Contract> contract = contractRepository.findByRoomIdAndStatus(roomId, Enum.ContractStatus.ACTIVE);
        if (contract.isEmpty()) {
            return List.of();
        }

        List<Invoice> invoices = invoiceRepository.findByContractId(contract.get().getId());

        return invoices.stream().map(invoice -> {
                    Optional<UtilityReading> reading = utilityReadingRepository.findAll().stream()
                            .filter(r -> r.getRoom().getId().equals(roomId) && r.getBillingCycle().equals(invoice.getBillingCycle()))
                            .findFirst();

                    Integer eUsage = 0;
                    Integer wUsage = 0;
                    String imgUrl = null;
                    LocalDate readingDate = null;

                    if (reading.isPresent()) {
                        UtilityReading r = reading.get();
                        eUsage = Boolean.TRUE.equals(r.getIsMeterReset()) ? r.getNewElectricity() : (r.getNewElectricity() - r.getOldElectricity());
                        wUsage = Boolean.TRUE.equals(r.getIsMeterReset()) ? r.getNewWater() : (r.getNewWater() - r.getOldWater());
                        imgUrl = r.getMeterImageUrl();
                        readingDate = r.getReadingDate();
                    }

                    return ServiceRoomDTO.InvoiceHistoryResponse.builder()
                            .invoiceId(invoice.getId())
                            .billingCycle(invoice.getBillingCycle())
                            .readingDate(readingDate)
                            .dueDate(invoice.getDueDate())
                            .totalAmount(invoice.getTotalAmount())
                            .status(invoice.getStatus().name())
                            .meterImageUrl(imgUrl)
                            .electricUsage(eUsage)
                            .waterUsage(wUsage)
                            .build();
                }).sorted((i1, i2) -> i2.getBillingCycle().compareTo(i1.getBillingCycle()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void confirmCashPayment(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        if (invoice.getStatus() == Enum.InvoiceStatus.PAID) {
            throw new RuntimeException("Hóa đơn này đã được thanh toán rồi!");
        }

        invoice.setStatus(Enum.InvoiceStatus.PAID);
        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        if (invoice.getContract().getTenant().getFcmToken() != null) {
            fcmNotificationService.sendPushNotification(
                    invoice.getContract().getTenant().getFcmToken(),
                    "Thanh toán thành công",
                    "Hóa đơn kỳ " + invoice.getBillingCycle() + " đã được chủ trọ xác nhận thanh toán tiền mặt thành công!"
            );
        }
    }

    private String generateRandomPaymentCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder("HD");
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }
}