package com.example.QLPT.Config;

import org.springframework.dao.DataAccessException;

/** Keep database internals out of API responses, including legacy controller catch blocks. */
public final class ApiErrors {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ApiErrors.class);
    private ApiErrors() {}
    public static String message(Exception error) {
        if (error instanceof DataAccessException) {
            LOG.error("Database operation failed", error);
            return "Dữ liệu không hợp lệ hoặc trùng với bản ghi đã tồn tại. Vui lòng kiểm tra lại.";
        }
        return error.getMessage();
    }
}
