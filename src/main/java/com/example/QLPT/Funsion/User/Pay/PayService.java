package com.example.QLPT.Funsion.User.Pay;

import com.example.QLPT.Config.Firebase.FcmNotificationService;
import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Invoice;
import com.example.QLPT.Repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayService {

    private final InvoiceRepository invoiceRepository;

    @Value("${payment.bank-id:MB}")
    private String bankId;

    @Value("${payment.account-no:0356196602}")
    private String accountNo;

    @Value("${payment.account-name:QUACH VAN KHAI}")
    private String accountName;

    private final FcmNotificationService fcmNotificationService;
    // 1. Tạo thông tin VietQR cho hóa đơn
    public PayDTO.VietQRResponse generateVietQR(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        if (invoice.getStatus() == Enum.InvoiceStatus.PAID) {
            throw new RuntimeException("Hóa đơn này đã được thanh toán!");
        }

        String transferContent = invoice.getPaymentCode();

        String qrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
                bankId,
                accountNo,
                invoice.getTotalAmount().toPlainString(),
                transferContent,
                accountName.replace(" ", "%20")
        );

        return PayDTO.VietQRResponse.builder()
                .invoiceId(invoiceId)
                .qrUrl(qrUrl)
                .bankId(bankId)
                .accountNo(accountNo)
                .accountName(accountName)
                .amount(invoice.getTotalAmount())
                .transferContent(transferContent)
                .build();
    }

    // 2. Xử lý Webhook từ SePay khi có tiền vào
    @Transactional
    public void processSePayWebhook(PayDTO.SePayWebhookRequest request) {
        System.out.println("========== BẮT ĐẦU XỬ LÝ WEBHOOK SEPAY ==========");
        System.out.println("1. Tiền vào: " + request.getAmountIn());
        System.out.println("1. Nội dung gốc: " + request.getTransactionContent());

        log.info("Nhận Webhook từ SePay: Tiền vào = {}, Nội dung = {}", request.getAmountIn(), request.getTransactionContent());

        if (request.getAmountIn() == null || request.getAmountIn().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("-> LỖI 1: Số tiền không hợp lệ (null hoặc <= 0). Hủy bỏ.");
            System.out.println("===================================================");
            return;
        }

        // Đề phòng transactionContent null gây lỗi NullPointerException
        String rawContent = request.getTransactionContent() != null ? request.getTransactionContent() : "";
        String content = rawContent.toUpperCase();
        System.out.println("2. Nội dung sau khi in hoa: " + content);

        // Đã sửa Regex: HD + đúng 6 ký tự (Chữ cái in hoa hoặc số)
        Pattern pattern = Pattern.compile("(HD[A-Z0-9]{6})");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String paymentCode = matcher.group(1);
            System.out.println("3. THÀNH CÔNG: Đã bóc tách được mã Hóa Đơn từ nội dung -> " + paymentCode);

            Optional<Invoice> invoiceOpt = invoiceRepository.findByPaymentCode(paymentCode);

            if (invoiceOpt.isPresent()) {
                Invoice invoice = invoiceOpt.get();
                System.out.println("4. THÀNH CÔNG: Đã tìm thấy Hóa Đơn trong DB! (ID Hóa Đơn: " + invoice.getId() + ")");
                System.out.println("   + Tiền cần thanh toán (DB): " + invoice.getTotalAmount());
                System.out.println("   + Tiền khách chuyển (SePay): " + request.getAmountIn());

                // So sánh tiền gửi và tiền cần trả
                int compareResult = request.getAmountIn().compareTo(invoice.getTotalAmount());
                System.out.println("5. Kết quả so sánh tiền: " + compareResult + " (>= 0 là đủ tiền)");

                if (compareResult >= 0) {
                    invoice.setStatus(Enum.InvoiceStatus.PAID);
                    invoiceRepository.save(invoice);
                    System.out.println("6. CHÍNH THỨC THÀNH CÔNG: Đã cập nhật trạng thái hóa đơn thành PAID!");
                    log.info("Đã thanh toán tự động thành công cho hóa đơn mã: {}", paymentCode);

                    // THÊM ĐOẠN NÀY ĐỂ BẮN THÔNG BÁO KHI SEPAY NHẬN TIỀN VÀ GẠCH NỢ TỰ ĐỘNG
                    if (invoice.getContract().getTenant().getFcmToken() != null) {
                        fcmNotificationService.sendPushNotification(
                                invoice.getContract().getTenant().getFcmToken(),
                                "Thanh toán tự động thành công",
                                "Hệ thống đã nhận được " + request.getAmountIn() + " VNĐ qua chuyển khoản. Hóa đơn kỳ " + invoice.getBillingCycle() + " đã được gạch nợ!"
                        );
                    }

                } else {
                    System.out.println("-> LỖI 2: Khách chuyển thiếu tiền! (Cần: " + invoice.getTotalAmount() + " - Nhận: " + request.getAmountIn() + ")");
                    log.warn("Khách chuyển thiếu tiền. Cần: {}, Nhận: {}", invoice.getTotalAmount(), request.getAmountIn());
                }
            } else {
                System.out.println("-> LỖI 3: Tìm thấy mã [" + paymentCode + "] nhưng KHÔNG có trong Database!");
                log.warn("Nhận được tiền nhưng không tìm thấy mã hóa đơn {} trong hệ thống!", paymentCode);
            }
        } else {
            System.out.println("-> LỖI 4: Regex không tìm thấy mã (HDxxxxxx) trong nội dung chuyển khoản!");
            log.warn("Không tìm thấy cú pháp hóa đơn (HD...) trong nội dung: {}", content);
        }
        System.out.println("========== KẾT THÚC XỬ LÝ WEBHOOK SEPAY ==========");
    }
}