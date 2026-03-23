package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.admin.*;

import java.util.List;

public interface AdminUserServiceInterface {

    CreateUserResponse createUser(CreateUserRequest request, String createdBy);

    BulkCreateUsersResponse bulkCreateUsers(BulkCreateUsersRequest request, String createdBy);

    List<UserDto> getAllUsers();

    List<UserDto> getUsersByRole(String roleName);

    UserDto getUserById(Integer id);

    UserDto updateUser(Integer id, UpdateUserRequest request);

    UserDto deactivateUser(Integer id, String reason);

    UserDto reactivateUser(Integer id);

    void activateAccount(String email, String newPassword);

    String resetPassword(Integer id);
}
