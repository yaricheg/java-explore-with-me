package ru.practicum.explorewithme.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@Data
@AllArgsConstructor
public class UserDto {

    @NotNull
    private Long id;

    @NotBlank
    @Email
    private String email;


    @NotBlank
    private String name;
}
