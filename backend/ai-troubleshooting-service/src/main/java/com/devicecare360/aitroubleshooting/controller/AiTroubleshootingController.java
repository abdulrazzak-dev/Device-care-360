package com.devicecare360.aitroubleshooting.controller;

import com.devicecare360.aitroubleshooting.document.TroubleshootingLog;
import com.devicecare360.aitroubleshooting.dto.TroubleshootingRequest;
import com.devicecare360.aitroubleshooting.dto.TroubleshootingResponse;
import com.devicecare360.aitroubleshooting.service.AiTroubleshootingService;
import com.devicecare360.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/troubleshooting")
@RequiredArgsConstructor
public class AiTroubleshootingController {

    private final AiTroubleshootingService troubleshootingService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<TroubleshootingResponse>> analyze(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "ANONYMOUS") String userId,
            @Valid @RequestBody TroubleshootingRequest request) {
        TroubleshootingResponse response = troubleshootingService.analyze(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<TroubleshootingResponse>> chat(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "ANONYMOUS") String userId,
            @Valid @RequestBody TroubleshootingRequest request) {
        TroubleshootingResponse response = troubleshootingService.analyze(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/category-search")
    public ResponseEntity<ApiResponse<String>> categorySearch(@RequestBody Map<String, String> payload) {
        String input = payload.getOrDefault("category", "Other");
        return ResponseEntity.ok(ApiResponse.success(input, "Category normalized"));
    }

    @PostMapping("/brand-search")
    public ResponseEntity<ApiResponse<String>> brandSearch(@RequestBody Map<String, String> payload) {
        String input = payload.getOrDefault("brand", "Generic");
        return ResponseEntity.ok(ApiResponse.success(input, "Brand normalized"));
    }

    @PostMapping("/issue-search")
    public ResponseEntity<ApiResponse<String>> issueSearch(@RequestBody Map<String, String> payload) {
        String input = payload.getOrDefault("issue", "General Fault");
        return ResponseEntity.ok(ApiResponse.success(input, "Issue classified"));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<TroubleshootingLog>>> getHistory(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(troubleshootingService.getHistory(userId)));
    }
}
