package com.example.traffic_light_backend.model;
public record TrafficStatus(
        LightColor currentLight,
        TrafficMode mode,
        int remainingSeconds,
        boolean running,
        boolean emergency,
        String message
) {
}