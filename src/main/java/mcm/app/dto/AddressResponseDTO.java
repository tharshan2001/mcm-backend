package mcm.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponseDTO {

    private Long id;
    private String fullName;
    private String phone;
    private String country;
    private String state;
    private String city;
    private String postalCode;
    private String addressLine;
    private Boolean isDefault;
}