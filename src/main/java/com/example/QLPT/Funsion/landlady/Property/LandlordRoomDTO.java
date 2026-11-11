package com.example.QLPT.Funsion.landlady.Property;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class LandlordRoomDTO {
    private Long id;
    private String roomNumber;
    private BigDecimal basePrice;
    private String status;
    private LandlordTenantDTO currentTenant; // Sẽ NULL nếu phòng đang trống (AVAILABLE)
}