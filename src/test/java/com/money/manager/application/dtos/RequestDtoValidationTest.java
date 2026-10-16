package com.money.manager.application.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Verifies the bean-validation constraints added to the request DTOs
 * (issue #96): required numeric fields, string length limits and the
 * category color format. Runs without a Spring context.
 */
class RequestDtoValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static Set<String> violatedFields(Object dto, Class<?>... groups) {
        return VALIDATOR.validate(dto, groups).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void debtRequest_withoutTotalAmount_isRejected() {
        DebtRequestDTO dto = new DebtRequestDTO("Car", null, "2026-01-01", null);

        assertThat(violatedFields(dto)).containsExactly("totalAmount");
    }

    @Test
    void debtRequest_withValidData_isAccepted() {
        DebtRequestDTO dto = new DebtRequestDTO("Car", 3000.0, "2026-01-01", null);

        assertThat(violatedFields(dto)).isEmpty();
    }

    @Test
    void paymentRequest_withoutAmount_isRejected() {
        PaymentRequestDTO dto = new PaymentRequestDTO("2026-01-15", null, true,
                new DebtDTO(10L, "Car", 3000.0, null, null));

        assertThat(violatedFields(dto)).containsExactly("amount");
    }

    @Test
    void paymentRequest_withValidData_isAccepted() {
        PaymentRequestDTO dto = new PaymentRequestDTO("2026-01-15", 500.0, true,
                new DebtDTO(10L, "Car", 3000.0, null, null));

        assertThat(violatedFields(dto)).isEmpty();
    }

    @Test
    void categoryRequest_withNonHexColor_isRejected() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Food", "red");

        assertThat(violatedFields(dto)).containsExactly("color");
    }

    @Test
    void categoryRequest_withHexColor_isAccepted() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Food", "#00FF00");

        assertThat(violatedFields(dto)).isEmpty();
    }

    @Test
    void userRequest_withoutPassword_passesDefaultGroup() {
        // PUT /user may omit the password to keep the current one.
        UserRequestDTO dto = new UserRequestDTO("javi", null, "javi@mail.com");

        assertThat(violatedFields(dto)).isEmpty();
    }

    @Test
    void userRequest_withoutPassword_failsCreateGroup() {
        UserRequestDTO dto = new UserRequestDTO("javi", null, "javi@mail.com");

        assertThat(violatedFields(dto, UserRequestDTO.OnCreate.class)).containsExactly("password");
    }

    @Test
    void userRequest_createGroup_stillEnforcesDefaultConstraints() {
        UserRequestDTO dto = new UserRequestDTO("", "secret", "not-an-email");

        assertThat(violatedFields(dto, UserRequestDTO.OnCreate.class))
                .containsExactlyInAnyOrder("username", "email");
    }
}
