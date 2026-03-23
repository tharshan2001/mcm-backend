package mcm.app.dto;

import java.util.List;

public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private List<CouponResponse> usedCoupons;
    private List<AddressResponseDTO> addresses;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<CouponResponse> getUsedCoupons() { return usedCoupons; }
    public void setUsedCoupons(List<CouponResponse> usedCoupons) { this.usedCoupons = usedCoupons; }

    public List<AddressResponseDTO> getAddresses() { return addresses; }
    public void setAddresses(List<AddressResponseDTO> addresses) { this.addresses = addresses; }
}