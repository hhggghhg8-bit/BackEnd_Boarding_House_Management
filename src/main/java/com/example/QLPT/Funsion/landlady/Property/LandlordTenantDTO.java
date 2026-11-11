package com.example.QLPT.Funsion.landlady.Property;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandlordTenantDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
}