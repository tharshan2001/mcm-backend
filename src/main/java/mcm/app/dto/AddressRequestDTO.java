package mcm.app.dto;

import lombok.Data;

@Data
public class AddressRequestDTO {

    private String fullName;
    private String phone;
    private String country;
    private String state;
    private String city;
    private String postalCode;
    private String addressLine;
    private Boolean isDefault;
}