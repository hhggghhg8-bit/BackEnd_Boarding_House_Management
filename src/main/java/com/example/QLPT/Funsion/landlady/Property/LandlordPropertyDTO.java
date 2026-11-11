package com.example.QLPT.Funsion.landlady.Property;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LandlordPropertyDTO {
    private Long id;
    private String name;
    private String address;
    private List<LandlordRoomDTO> rooms;
}
