package com.example.traffic_light_backend.service;

import com.example.traffic_light_backend.exception.InvalidLightException;
import com.example.traffic_light_backend.exception.InvalidStateOperationException;
import com.example.traffic_light_backend.model.LightColor;
import com.example.traffic_light_backend.model.TrafficMode;
import com.example.traffic_light_backend.model.TrafficStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TrafficLightService {

    private static final int RED_DURATION = 10;
    private static final int GREEN_DURATION = 8;
    private static final int YELLOW_DURATION = 3;

    // In-memory state initialized to S1 defaults
    private LightColor currentLight = LightColor.RED;
    private TrafficMode mode = TrafficMode.STOPPED;
    private int remainingSeconds = 0;
    private boolean running = false;
    private boolean emergency = false;
    private String message = "System initialized (STOPPED)";

    public synchronized TrafficStatus getStatus() {
        return snapshot();
    }

    public synchronized TrafficStatus start() {
        if (emergency || mode == TrafficMode.EMERGENCY) {
            throw new InvalidStateOperationException(
                    "Emergency override active. Reset is required before starting.");
        }
        if (mode == TrafficMode.AUTOMATIC || mode == TrafficMode.PAUSED) {
            throw new InvalidStateOperationException(
                    "Automatic cycle is already active or paused.");
        }
        if (mode == TrafficMode.MANUAL) {
            throw new InvalidStateOperationException(
                    "Cannot start automatic cycle in MANUAL mode. Press Stop or Reset first.");
        }

        currentLight = LightColor.RED;
        mode = TrafficMode.AUTOMATIC;
        remainingSeconds = RED_DURATION;
        running = true;
        emergency = false;
        message = "Normal cycle";
        return snapshot();
    }

    public synchronized TrafficStatus pause() {
        if (mode != TrafficMode.AUTOMATIC || !running) {
            throw new InvalidStateOperationException(
                    "Cannot pause: automatic cycle is not currently running.");
        }

        mode = TrafficMode.PAUSED;
        running = false;
        message = "Cycle paused at " + remainingSeconds + "s";
        return snapshot();
    }

    public synchronized TrafficStatus resume() {
        if (mode != TrafficMode.PAUSED) {
            throw new InvalidStateOperationException(
                    "Cannot resume: traffic light is not paused.");
        }

        mode = TrafficMode.AUTOMATIC;
        running = true;
        message = "Cycle resumed";
        return snapshot();
    }

    public synchronized TrafficStatus stop() {
        if (emergency || mode == TrafficMode.EMERGENCY) {
            throw new InvalidStateOperationException(
                    "Cannot stop during emergency override. Press Reset to clear emergency state.");
        }

        currentLight = LightColor.RED;
        mode = TrafficMode.STOPPED;
        remainingSeconds = 0;
        running = false;
        emergency = false;
        message = "Cycle stopped";
        return snapshot();
    }

    public synchronized TrafficStatus reset() {
        currentLight = LightColor.RED;
        mode = TrafficMode.STOPPED;
        remainingSeconds = 0;
        running = false;
        emergency = false;
        message = "System reset to initial state";
        return snapshot();
    }

    public synchronized TrafficStatus emergency() {
        currentLight = LightColor.RED;
        mode = TrafficMode.EMERGENCY;
        remainingSeconds = 0;
        running = false;
        emergency = true;
        message = "Emergency override active. Press Reset to restore.";
        return snapshot();
    }

    public synchronized TrafficStatus setManualLight(String lightInput) {
        LightColor selectedLight = parseLight(lightInput);

        if (emergency || mode == TrafficMode.EMERGENCY) {
            throw new InvalidStateOperationException(
                    "Emergency override active. Reset is required before manual control.");
        }
        if (mode != TrafficMode.STOPPED && mode != TrafficMode.MANUAL) {
            throw new InvalidStateOperationException(
                    "Manual control is only allowed when the cycle is stopped.");
        }

        currentLight = selectedLight;
        mode = TrafficMode.MANUAL;
        remainingSeconds = 0;
        running = false;
        emergency = false;
        message = "Manual mode: " + selectedLight;
        return snapshot();
    }

    @Scheduled(fixedRate = 1000)
    public synchronized void tick() {
        if (mode != TrafficMode.AUTOMATIC || !running || emergency) {
            return;
        }

        if (remainingSeconds > 1) {
            remainingSeconds--;
        } else {
            advanceLight();
        }
    }

    private void advanceLight() {
        switch (currentLight) {
            case RED -> {
                currentLight = LightColor.GREEN;
                remainingSeconds = GREEN_DURATION;
            }
            case GREEN -> {
                currentLight = LightColor.YELLOW;
                remainingSeconds = YELLOW_DURATION;
            }
            case YELLOW -> {
                currentLight = LightColor.RED;
                remainingSeconds = RED_DURATION;
            }
        }
        message = "Normal cycle";
    }

    private LightColor parseLight(String input) {
        if (input == null || input.isBlank()) {
            throw new InvalidLightException("Light color must be RED, YELLOW, or GREEN.");
        }
        try {
            return LightColor.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidLightException(
                    "Invalid light '" + input + "'. Allowed values: RED, YELLOW, GREEN.");
        }
    }

    private TrafficStatus snapshot() {
        return new TrafficStatus(
                currentLight,
                mode,
                remainingSeconds,
                running,
                emergency,
                message
        );
    }
}