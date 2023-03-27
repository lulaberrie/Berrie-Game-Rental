package com.berrie.gamerental.model;

public enum Genre {
    ACTION,
    ADVENTURE,
    BATTLE_ROYAL,
    RPG,
    SIMULATION,
    SPORTS,
    SHOOTER;

    @Override
    public String toString() {
        return name().replace("_", " ");
    }
}
