package com.example.traffic_light_backend.controller;


import com.example.traffic_light_backend.model.TrafficStatus;
import com.example.traffic_light_backend.service.TrafficLightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/traffic")
public class TrafficLightController {

    private final TrafficLightService trafficLightService;

    public TrafficLightController(TrafficLightService trafficLightService) {
        this.trafficLightService = trafficLightService;
    }

    @GetMapping("/status")
    public ResponseEntity<TrafficStatus> getStatus() {
        return ResponseEntity.ok(trafficLightService.getStatus());
    }

    @PostMapping("/start")
    public ResponseEntity<TrafficStatus> start() {
        return ResponseEntity.ok(trafficLightService.start());
    }

    @PostMapping("/pause")
    public ResponseEntity<TrafficStatus> pause() {
        return ResponseEntity.ok(trafficLightService.pause());
    }

    @PostMapping("/resume")
    public ResponseEntity<TrafficStatus> resume() {
        return ResponseEntity.ok(trafficLightService.resume());
    }

    @PostMapping("/stop")
    public ResponseEntity<TrafficStatus> stop() {
        return ResponseEntity.ok(trafficLightService.stop());
    }

    @PostMapping("/reset")
    public ResponseEntity<TrafficStatus> reset() {
        return ResponseEntity.ok(trafficLightService.reset());
    }

    @PostMapping("/emergency")
    public ResponseEntity<TrafficStatus> emergency() {
        return ResponseEntity.ok(trafficLightService.emergency());
    }

    @PostMapping("/manual/{light}")
    public ResponseEntity<TrafficStatus> setManualLight(@PathVariable String light) {
        return ResponseEntity.ok(trafficLightService.setManualLight(light));
    }
}
