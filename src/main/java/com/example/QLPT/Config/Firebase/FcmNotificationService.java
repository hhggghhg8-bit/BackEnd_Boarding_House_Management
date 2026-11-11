package com.example.QLPT.Config.Firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class FcmNotificationService {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // ĐÃ SỬA: Cập nhật đường dẫn chuẩn khớp với cây thư mục của bạn
                InputStream serviceAccount = new ClassPathResource("firebase/firebase-service-account.json").getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("========== FIREBASE KẾT NỐI THÀNH CÔNG ==========");
            }
        } catch (Exception e) {
            System.err.println("========== LỖI KẾT NỐI FIREBASE ==========");
            e.printStackTrace();
        }
    }

    public void sendPushNotification(String targetToken, String title, String body) {
        if (targetToken == null || targetToken.isEmpty()) {
            System.out.println("-> Lỗi gửi FCM: Token thiết bị bị rỗng!");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            // Gửi và nhận về response ID của Firebase để log ra kiểm tra
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("-> Đã gửi thông báo FCM thành công! Message ID: " + response);

        } catch (Exception e) {
            System.err.println("-> Lỗi gửi thông báo FCM: " + e.getMessage());
        }
    }
}