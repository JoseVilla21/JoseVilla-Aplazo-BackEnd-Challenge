package com.test.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String lastName;

    @NotBlank(message = "El segundo apellido es obligatorio")
    private String secondLastNme;

    @NotBlank(message = "La fecha de nacimiento es obligatoria")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "El formato de fecha debe ser yyyy-MM-dd")
    private String dateOfBirth;
}
