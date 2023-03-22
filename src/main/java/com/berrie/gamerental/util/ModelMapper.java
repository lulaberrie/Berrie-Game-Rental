package com.berrie.gamerental.util;

import com.berrie.gamerental.dto.AuthenticationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelMapper {

    public static AuthenticationResponse toAuthenticationResponse(String token) {
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    public static <T> T fromJson(String json, Class<T> tclass) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, tclass);
    }
}
