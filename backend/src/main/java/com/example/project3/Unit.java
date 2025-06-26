package com.example.project3;

public enum Unit {
    KG("kg"),
    PIECE("piece"),
    PACK("pack"),
    DOZEN("dozen"),
    LITER("liter"),
    GRAM("gram"),
    POUND("pound");

    private final String displayName;

    Unit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 