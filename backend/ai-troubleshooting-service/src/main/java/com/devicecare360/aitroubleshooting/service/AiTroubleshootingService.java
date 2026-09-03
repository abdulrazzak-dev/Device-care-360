package com.devicecare360.aitroubleshooting.service;

import com.devicecare360.aitroubleshooting.config.RabbitMQConfig;
import com.devicecare360.aitroubleshooting.document.TroubleshootingLog;
import com.devicecare360.aitroubleshooting.dto.TroubleshootingRequest;
import com.devicecare360.aitroubleshooting.dto.TroubleshootingResponse;
import com.devicecare360.aitroubleshooting.repository.TroubleshootingLogRepository;
import com.devicecare360.aitroubleshooting.safety.SafetyEngine;
import com.devicecare360.shared.event.HighRiskIssueDetectedEvent;
import com.devicecare360.shared.event.TroubleshootingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTroubleshootingService {

    private final GeminiClient geminiClient;
    private final SafetyEngine safetyEngine;
    private final TroubleshootingLogRepository logRepository;
    private final RabbitTemplate rabbitTemplate;

    public TroubleshootingResponse analyze(String userId, TroubleshootingRequest request) {
        TroubleshootingResponse initialResponse = geminiClient.generateTroubleshooting(request);

        SafetyEngine.TroubleshootingResult safetyResult = safetyEngine.evaluateSafety(
                request.getCategory(), request.getIssueDescription(), initialResponse
        );

        TroubleshootingResponse finalResponse = safetyResult.response();

        TroubleshootingLog logEntity = TroubleshootingLog.builder()
                .userId(userId)
                .category(request.getCategory())
                .brand(request.getBrand())
                .model(request.getModel())
                .issueDescription(request.getIssueDescription())
                .response(finalResponse)
                .highRiskDetected(safetyResult.highRisk())
                .hazardCategory(safetyResult.hazardCategory())
                .timestamp(LocalDateTime.now())
                .build();

        TroubleshootingLog savedLog = logRepository.save(logEntity);

        TroubleshootingCompletedEvent completedEvent = TroubleshootingCompletedEvent.builder()
                .troubleshootingId(savedLog.getId())
                .userId(userId)
                .category(request.getCategory())
                .brand(request.getBrand())
                .issueDescription(request.getIssueDescription())
                .riskLevel(finalResponse.getRiskLevel())
                .requiresProfessional(finalResponse.isRequiresProfessional())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TROUBLESHOOTING_COMPLETED_ROUTING_KEY, completedEvent);

        if (safetyResult.highRisk()) {
            HighRiskIssueDetectedEvent highRiskEvent = HighRiskIssueDetectedEvent.builder()
                    .troubleshootingId(savedLog.getId())
                    .userId(userId)
                    .category(request.getCategory())
                    .brand(request.getBrand())
                    .issueDescription(request.getIssueDescription())
                    .riskLevel(finalResponse.getRiskLevel())
                    .hazardCategory(safetyResult.hazardCategory())
                    .recommendedAction(finalResponse.getRecommendedAction())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.HIGH_RISK_DETECTED_ROUTING_KEY, highRiskEvent);
        }

        return finalResponse;
    }

    public List<TroubleshootingLog> getHistory(String userId) {
        return logRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
