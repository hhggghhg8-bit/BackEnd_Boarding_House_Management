# Báo cáo sửa lỗi và kiểm thử QLPT

Ngày kiểm tra: 09/11/2026. Môi trường: Java 21.0.11, Spring Boot 3.3.4, PostgreSQL 18.

## Lỗi đã tái hiện và sửa

1. `users.fcm_token` là `varchar(255)`: API lưu token 300 ký tự trả lỗi database. Đổi entity và cột đang dùng sang `text`; bỏ giới hạn 255 trong DTO đăng nhập. Kiểm tra lưu token 300 ký tự và đăng nhập với token 400 ký tự đều thành công.
2. `@SQLRestriction` trên Room khiến hợp đồng/hóa đơn cũ không tải được phòng đã xóa mềm, phát sinh `JpaObjectRetrievalFailureException: Unable to find Room`. Chuyển điều kiện lọc sang các truy vấn quản lý trong RoomRepository để các liên kết lịch sử vẫn đọc được. Test xóa phòng có hóa đơn, đọc lại lịch sử, danh sách người dùng, tạo lại số phòng và đọc dịch vụ phòng mới đều thành công.
3. API tạo phòng nhận giá âm hoặc dữ liệu thiếu. Bổ sung Bean Validation cho dữ liệu tạo/sửa khu trọ, phòng, người dùng, hồ sơ, dịch vụ và điện nước; kích hoạt `@Valid` ở controller. Chuỗi vượt độ dài cột được từ chối trước khi ghi SQL. Lỗi truy cập database được ghi log phía server và trả thông báo không chứa SQL cho client.
4. Hai API trả phòng bỏ sót công nợ hoặc không kiểm tra nợ. Cả hai hiện chặn `UNPAID`, `PARTIALLY_PAID`, `OVERDUE`; test xác nhận phòng và hợp đồng không đổi sau khi bị từ chối.
5. Hợp đồng tạo qua API manage có thể thiếu ngày bắt đầu và làm hỏng việc sắp xếp lịch sử. Bổ sung ngày mặc định, tiền cọc/trạng thái cọc mặc định và ngày trả phòng thực tế. Cập nhật User + TenantProfile trong cùng transaction.

Đây là các lỗi tái hiện được trong kiểm thử; chưa có payload hoặc log thao tác ban đầu để khẳng định lỗi nào trùng hoàn toàn với lần lỗi được báo.

## Kết quả

**18 ca chạy, 0 thất bại, 0 lỗi, 0 bỏ qua.** Test API dùng MockMvc với Spring Security và repository/JPA/PostgreSQL thật. Không dùng H2 hoặc mock database. Mỗi lần chạy tạo schema ngẫu nhiên `qlpt_test_*` rồi tự xóa, không ghi vào bảng `public` của ứng dụng. Firebase được mock để không gửi thông báo ra ngoài.

Các workflow thành công đi qua toàn bộ **35 route HTTP** hiện có:

| Nhóm API | Số route | Nội dung kiểm tra |
| --- | ---: | --- |
| `/api/auth` | 3 | Đăng ký, đăng nhập, xóa FCM khi đăng xuất; thêm sai mật khẩu và JWT thật |
| `/api/profile` và `/api/tenant` | 5 | Hồ sơ, cập nhật hồ sơ, phòng đang thuê, hóa đơn, lưu FCM |
| `/api/landlord/manage` | 6 | Tạo khu trọ, tạo phòng đơn/hàng loạt, sửa phòng, thêm khách, trả phòng |
| `/api/landlord/properties` | 5 | Dashboard, sửa/xóa phòng, thêm và trả khách qua luồng thứ hai |
| `/api/landlord/room-services` | 9 | Danh mục, gán/sửa giá/xóa dịch vụ, tổng hợp, ghi điện nước kèm ảnh, lịch sử, thu tiền mặt |
| `/api/landlady/users` | 5 | Tạo, sửa, danh sách, chi tiết, lịch sử người dùng |
| `/api/pay` | 2 | Thông tin QR, xử lý payload webhook giả lập: thiếu tiền, đủ tiền, gửi lại |

Các ca bổ sung: thiếu trường bắt buộc; giá âm; chuỗi quá dài; tạo phòng trùng; rollback khi tạo phòng hàng loạt thất bại; chặn vai trò không phù hợp; đọc lịch sử sau xóa mềm; migration giữ token cũ và chạy lại được.

Ảnh meter do test tạo được xóa sau workflow. Sau lần chạy cuối không còn schema test.

## Database hiện tại

Đã chạy `maintenance/fix-fcm-token.sql` trên `qlpt_db` với `ON_ERROR_STOP` và thời gian chờ khóa 5 giây. Cột hiện là `text`. Số người dùng trước/sau: **9/9**, số token có giá trị: **0/0**. Migration chỉ mở rộng kiểu cột, không sửa/xóa bản ghi.

## Chạy lại

Dùng JDK 21 và chạy `mvn test` hoặc `./mvnw.cmd test`. Cần tài khoản PostgreSQL có quyền tạo/xóa schema trong database kiểm thử. Có thể đặt `TEST_DATABASE_URL`, `TEST_DATABASE_USERNAME`, `TEST_DATABASE_PASSWORD`; nếu không đặt, test đọc cấu hình datasource trong application.properties. Báo cáo JUnit nằm tại `target/surefire-reports/com.example.QLPT.QlptApplicationTests.txt`.

Trong môi trường kiểm tra này Maven wrapper không khởi động được từ shell tự động, nên đã gọi Maven 3.10.0 có sẵn trong cache, với `-Dmaven.repo.local=C:/Users/qkhai/.m2/repository -o -q test`. Bộ test chạy và biên dịch bằng JDK 21.

## Giới hạn

Đã kiểm thử các API backend và các trường hợp hồi quy nêu trên, không phải mọi tổ hợp đầu vào hoặc kiểm thử tải/đồng thời. Chưa chạy giao diện mobile/frontend. Test thanh toán dùng payload giả lập tại handler, không xác nhận giao dịch SePay thật hay tải ảnh từ VietQR; Firebase không gửi push thật. Cần khởi động lại backend đang chạy để nạp mã Java mới.
