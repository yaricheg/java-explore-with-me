package ru.practicum.explorewithme.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {

    @Size(min = 2, max = 250)
    @NotBlank
    private String name;

    @Size(min = 6, max = 254)
    @Email
    @NotBlank
    private String email;

}
