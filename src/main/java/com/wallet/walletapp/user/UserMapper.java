package com.wallet.walletapp.user;

import com.wallet.walletapp.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        return response;
    }

    public UserResponse toResponse(UserReadProjection projection) {
        UserResponse response = new UserResponse();
        response.setUsername(projection.getUsername());
        response.setRole(projection.getRole().name());
        response.setTenantName(projection.getTenantName());
        return response;
    }
}
