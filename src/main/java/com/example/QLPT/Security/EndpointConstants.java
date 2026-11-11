package com.example.QLPT.Security;

public class EndpointConstants {

    // ==========================================
    // 1. PUBLIC ENDPOINTS (Truy cập tự do)
    // ==========================================
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/api/public/**",
            "/images/**",
            // BẠN ĐANG DÙNG CÁI NÀY TRONG PAYCONTROLLER:
            "/api/pay/sepay-webhook",
            // CÁI CŨ CỦA BẠN (CÓ THỂ XÓA NẾU KHÔNG DÙNG):
            // "/api/webhook/payment/**",
            "/uploads/**",
    };

    // ==========================================
    // 2. ADMIN ENDPOINTS (Chỉ Quản trị viên)
    // ==========================================
    public static final String[] ADMIN_ENDPOINTS = {
            "/api/admin/**"
    };

    // ==========================================
    // 3. LANDLORD ENDPOINTS (Chỉ Chủ trọ)
    // ==========================================
    public static final String[] LANDLORD_ENDPOINTS = {
            "/api/landlord/**",
            "/api/landlady/**"
    };

    // ==========================================
    // 4. TENANT ENDPOINTS (Chỉ Người thuê)
    // ==========================================
    public static final String[] TENANT_ENDPOINTS = {
            // API lấy mã QR "/api/pay/vietqr/**" CẦN YÊU CẦU ĐĂNG NHẬP (LẤY TOKEN) NÊN PHẢI BỎ VÀO ĐÂY
            "/api/tenant/**",
            "/api/profile/**",
            "/api/pay/vietqr/**"
    };
}