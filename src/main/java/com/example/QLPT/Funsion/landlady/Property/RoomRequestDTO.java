package com.example.QLPT.Funsion.landlady.Property;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RoomRequestDTO {
    @NotBlank @Size(max = 255)
    private String roomNumber;
    @NotNull @PositiveOrZero
    private BigDecimal basePrice;
}