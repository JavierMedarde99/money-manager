package com.money.manager.application.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

public record UserRequestDTO(@NotBlank @Size(max = 255) String username,
        @NotBlank(groups = OnCreate.class) @Size(max = 72) String password,
        @NotBlank @Email @Size(max = 255) String email) {

    /**
     * Group used only when creating a user (POST /user): the password is
     * mandatory then, but optional when updating an existing profile.
     */
    public interface OnCreate extends Default {
    }
}
