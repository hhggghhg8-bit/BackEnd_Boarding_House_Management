package com.example.QLPT.Funsion.landlady.Property;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssignTenantRequestDTO {
    @NotNull @Positive
    private Long tenantId;            // ID của tài khoản người thuê
    @PositiveOrZero
    private BigDecimal depositAmount; // Tiền cọc
    private LocalDate startDate;      // Ngày dọn vào
}