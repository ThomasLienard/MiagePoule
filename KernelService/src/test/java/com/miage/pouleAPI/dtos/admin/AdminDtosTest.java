package com.miage.pouleAPI.dtos.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests des DTOs Admin")
class AdminDtosTest {

    @Test
    @DisplayName("UserDto devrait stocker et retourner toutes les valeurs")
    void userDto_shouldStoreAndReturnAllValues() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime deactivatedAt = LocalDateTime.now().plusDays(1);
        
        UserDto dto = new UserDto(
            1, "John", "Doe", "john@test.com", "ATHLETE", "FR",
            true, true, false, createdAt, "admin@test.com",
            deactivatedAt, "Raison test"
        );

        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.name()).isEqualTo("John");
        assertThat(dto.lastname()).isEqualTo("Doe");
        assertThat(dto.email()).isEqualTo("john@test.com");
        assertThat(dto.roleName()).isEqualTo("ATHLETE");
        assertThat(dto.countryCode()).isEqualTo("FR");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.isAccountActivated()).isTrue();
        assertThat(dto.mustChangePassword()).isFalse();
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.createdBy()).isEqualTo("admin@test.com");
        assertThat(dto.deactivatedAt()).isEqualTo(deactivatedAt);
        assertThat(dto.deactivationReason()).isEqualTo("Raison test");
    }

    @Test
    @DisplayName("UserDto devrait gérer les valeurs nulles")
    void userDto_shouldHandleNullValues() {
        UserDto dto = new UserDto(
            null, null, null, null, null, null,
            null, null, null, null, null, null, null
        );

        assertThat(dto.id()).isNull();
        assertThat(dto.name()).isNull();
        assertThat(dto.lastname()).isNull();
        assertThat(dto.email()).isNull();
        assertThat(dto.roleName()).isNull();
        assertThat(dto.countryCode()).isNull();
        assertThat(dto.isActive()).isNull();
    }

    @Test
    @DisplayName("CreateUserRequest devrait stocker toutes les valeurs")
    void createUserRequest_shouldStoreAllValues() {
        CreateUserRequest request = new CreateUserRequest(
            "John", "Doe", "john@test.com", "ATHLETE", "FR"
        );

        assertThat(request.name()).isEqualTo("John");
        assertThat(request.lastname()).isEqualTo("Doe");
        assertThat(request.email()).isEqualTo("john@test.com");
        assertThat(request.roleName()).isEqualTo("ATHLETE");
        assertThat(request.countryCode()).isEqualTo("FR");
    }

    @Test
    @DisplayName("CreateUserResponse devrait stocker toutes les valeurs")
    void createUserResponse_shouldStoreAllValues() {
        CreateUserResponse response = new CreateUserResponse(
            1, "John", "Doe", "john@test.com", "ATHLETE", "temp123", "Success"
        );

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("John");
        assertThat(response.lastname()).isEqualTo("Doe");
        assertThat(response.email()).isEqualTo("john@test.com");
        assertThat(response.roleName()).isEqualTo("ATHLETE");
        assertThat(response.temporaryPassword()).isEqualTo("temp123");
        assertThat(response.message()).isEqualTo("Success");
    }

    @Test
    @DisplayName("UpdateUserRequest devrait stocker toutes les valeurs")
    void updateUserRequest_shouldStoreAllValues() {
        UpdateUserRequest request = new UpdateUserRequest(
            "NewName", "NewLastname", "new@test.com", "SPECTATEUR", "DE"
        );

        assertThat(request.name()).isEqualTo("NewName");
        assertThat(request.lastname()).isEqualTo("NewLastname");
        assertThat(request.email()).isEqualTo("new@test.com");
        assertThat(request.roleName()).isEqualTo("SPECTATEUR");
        assertThat(request.countryCode()).isEqualTo("DE");
    }

    @Test
    @DisplayName("UpdateUserRequest devrait gérer les valeurs nulles")
    void updateUserRequest_shouldHandleNullValues() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null);

        assertThat(request.name()).isNull();
        assertThat(request.lastname()).isNull();
        assertThat(request.email()).isNull();
        assertThat(request.roleName()).isNull();
        assertThat(request.countryCode()).isNull();
    }

    @Test
    @DisplayName("DeactivateUserRequest devrait stocker la raison")
    void deactivateUserRequest_shouldStoreReason() {
        DeactivateUserRequest request = new DeactivateUserRequest("Violation des règles");

        assertThat(request.reason()).isEqualTo("Violation des règles");
    }

    @Test
    @DisplayName("ActivateAccountRequest devrait stocker le nouveau mot de passe")
    void activateAccountRequest_shouldStoreNewPassword() {
        ActivateAccountRequest request = new ActivateAccountRequest("newPassword123");

        assertThat(request.newPassword()).isEqualTo("newPassword123");
    }

    @Test
    @DisplayName("UserDto equals et hashCode")
    void userDto_equalsAndHashCode() {
        LocalDateTime time = LocalDateTime.now();
        UserDto dto1 = new UserDto(1, "John", "Doe", "john@test.com", "ATHLETE", "FR",
            true, true, false, time, "admin", null, null);
        UserDto dto2 = new UserDto(1, "John", "Doe", "john@test.com", "ATHLETE", "FR",
            true, true, false, time, "admin", null, null);
        UserDto dto3 = new UserDto(2, "Jane", "Doe", "jane@test.com", "ATHLETE", "FR",
            true, true, false, time, "admin", null, null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("CreateUserRequest equals et hashCode")
    void createUserRequest_equalsAndHashCode() {
        CreateUserRequest req1 = new CreateUserRequest("John", "Doe", "john@test.com", "ATHLETE", "FR");
        CreateUserRequest req2 = new CreateUserRequest("John", "Doe", "john@test.com", "ATHLETE", "FR");
        CreateUserRequest req3 = new CreateUserRequest("Jane", "Doe", "jane@test.com", "ATHLETE", "FR");

        assertThat(req1).isEqualTo(req2);
        assertThat(req1).isNotEqualTo(req3);
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
    }

    @Test
    @DisplayName("CreateUserResponse equals et hashCode")
    void createUserResponse_equalsAndHashCode() {
        CreateUserResponse res1 = new CreateUserResponse(1, "John", "Doe", "john@test.com", "ATHLETE", "temp", "ok");
        CreateUserResponse res2 = new CreateUserResponse(1, "John", "Doe", "john@test.com", "ATHLETE", "temp", "ok");
        CreateUserResponse res3 = new CreateUserResponse(2, "Jane", "Doe", "jane@test.com", "ATHLETE", "temp", "ok");

        assertThat(res1).isEqualTo(res2);
        assertThat(res1).isNotEqualTo(res3);
        assertThat(res1.hashCode()).isEqualTo(res2.hashCode());
    }

    @Test
    @DisplayName("UpdateUserRequest equals et hashCode")
    void updateUserRequest_equalsAndHashCode() {
        UpdateUserRequest req1 = new UpdateUserRequest("John", "Doe", "john@test.com", "ATHLETE", "FR");
        UpdateUserRequest req2 = new UpdateUserRequest("John", "Doe", "john@test.com", "ATHLETE", "FR");
        UpdateUserRequest req3 = new UpdateUserRequest("Jane", "Doe", "jane@test.com", "ATHLETE", "FR");

        assertThat(req1).isEqualTo(req2);
        assertThat(req1).isNotEqualTo(req3);
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
    }

    @Test
    @DisplayName("DeactivateUserRequest equals et hashCode")
    void deactivateUserRequest_equalsAndHashCode() {
        DeactivateUserRequest req1 = new DeactivateUserRequest("reason1");
        DeactivateUserRequest req2 = new DeactivateUserRequest("reason1");
        DeactivateUserRequest req3 = new DeactivateUserRequest("reason2");

        assertThat(req1).isEqualTo(req2);
        assertThat(req1).isNotEqualTo(req3);
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
    }

    @Test
    @DisplayName("ActivateAccountRequest equals et hashCode")
    void activateAccountRequest_equalsAndHashCode() {
        ActivateAccountRequest req1 = new ActivateAccountRequest("password1");
        ActivateAccountRequest req2 = new ActivateAccountRequest("password1");
        ActivateAccountRequest req3 = new ActivateAccountRequest("password2");

        assertThat(req1).isEqualTo(req2);
        assertThat(req1).isNotEqualTo(req3);
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
    }

    @Test
    @DisplayName("UserDto toString devrait contenir les valeurs")
    void userDto_toStringShouldContainValues() {
        UserDto dto = new UserDto(1, "John", "Doe", "john@test.com", "ATHLETE", "FR",
            true, true, false, null, "admin", null, null);

        String str = dto.toString();
        assertThat(str).contains("John");
        assertThat(str).contains("Doe");
        assertThat(str).contains("john@test.com");
    }

    @Test
    @DisplayName("CreateUserRequest toString devrait contenir les valeurs")
    void createUserRequest_toStringShouldContainValues() {
        CreateUserRequest request = new CreateUserRequest("John", "Doe", "john@test.com", "ATHLETE", "FR");

        String str = request.toString();
        assertThat(str).contains("John");
        assertThat(str).contains("Doe");
        assertThat(str).contains("john@test.com");
    }
}
