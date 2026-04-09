
package com.capgemini.homeloanmanagementapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String fullName;
    private String email;
}
