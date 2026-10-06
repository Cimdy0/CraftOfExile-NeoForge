package com.cimdy.coe.data.codec.support.meta;

import com.mojang.serialization.Codec;

import java.util.Map;

public record Status(Map<String, Boolean> status) {
    public static final Status EMPTY = new Status(Map.of());
    public static Codec<Status> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).xmap(Status::new, Status::status);

    public Status setStatus(String key, boolean value) {
        this.status.put(key, value);
        return new Status(status);
    }

    public static class Common {
        public static final String IDENTIFIED = "identified";
        public static final String FRACTURED = "fractured";
        public static final String CORRUPTED = "corrupted";
        public static final String SANCTIFIED = "sanctified";
        public static final String UNREVEALED = "unrevealed";
        public static final String DESECRATED = "desecrated";
        public static final String FORESEEING = "foreseeing";
        public static final String MIRRORED = "mirrored";
    }
}
