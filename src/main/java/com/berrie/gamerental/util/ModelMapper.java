package com.berrie.gamerental.util;

import com.berrie.gamerental.dto.AuthenticationResponse;

public class ModelMapper {

    public static AuthenticationResponse toAuthenticationResponse(String token) {
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}
