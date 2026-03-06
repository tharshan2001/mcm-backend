package mcm.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class AuthMeResponse {

    private String fullName;
    private String email;
    private Set<String> roles;
}