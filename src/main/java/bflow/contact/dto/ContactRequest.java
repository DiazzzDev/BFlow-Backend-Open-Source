package bflow.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(max = 150)
        String subject,

        @NotBlank
        @Size(max = 5000)
        String message
) {
}
