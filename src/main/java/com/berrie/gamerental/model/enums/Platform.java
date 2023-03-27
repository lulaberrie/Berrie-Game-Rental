package com.berrie.gamerental.model.enums;

public enum Platform {
    PC,
    PS4,
    PS5,
    NINTENDO_SWITCH,
    XBOX_ONE,
    XBOX_360;

    @Override
    public String toString() {
        return name().replace("_", " ");
    }
}
