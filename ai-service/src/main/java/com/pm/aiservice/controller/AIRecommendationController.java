package com.pm.aiservice.controller;

import com.pm.aiservice.model.AIRecommendation;
import com.pm.aiservice.repository.AIRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/ai-recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIRecommendationController {
    
    private final AIRecommendationRepository recommendationRepository;
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AIRecommendation>> getRecommendationsByPatient(@PathVariable String patientId) {
        log.info("Fetching AI recommendations for patient: {}", patientId);
        List<AIRecommendation> recommendations = recommendationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/patient/{patientId}/type/{type}")
    public ResponseEntity<List<AIRecommendation>> getRecommendationsByPatientAndType(
            @PathVariable String patientId, 
            @PathVariable AIRecommendation.RecommendationType type) {
        log.info("Fetching AI recommendations for patient: {} and type: {}", patientId, type);
        List<AIRecommendation> recommendations = recommendationRepository.findByPatientIdAndTypeOrderByCreatedAtDesc(patientId, type);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/patient/{patientId}/priority/{priority}")
    public ResponseEntity<List<AIRecommendation>> getRecommendationsByPatientAndPriority(
            @PathVariable String patientId, 
            @PathVariable String priority) {
        log.info("Fetching AI recommendations for patient: {} with priority: {}", patientId, priority);
        List<AIRecommendation> recommendations = recommendationRepository.findByPatientIdAndPriorityOrderByCreatedAtDesc(patientId, priority);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/patient/{patientId}/recent")
    public ResponseEntity<List<AIRecommendation>> getRecentRecommendationsByPatient(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("Fetching recent AI recommendations for patient: {} (last {} days)", patientId, days);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<AIRecommendation> recommendations = recommendationRepository.findByPatientIdAndCreatedAtAfter(patientId, since);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/{recommendationId}")
    public ResponseEntity<AIRecommendation> getRecommendationById(@PathVariable String recommendationId) {
        log.info("Fetching AI recommendation by ID: {}", recommendationId);
        Optional<AIRecommendation> recommendation = recommendationRepository.findById(recommendationId);
        return recommendation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/type/{type}/recent")
    public ResponseEntity<List<AIRecommendation>> getRecentRecommendationsByType(
            @PathVariable AIRecommendation.RecommendationType type,
            @RequestParam(defaultValue = "24") int hours) {
        log.info("Fetching recent AI recommendations by type: {} (last {} hours)", type, hours);
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<AIRecommendation> recommendations = recommendationRepository.findByTypeAndCreatedAtAfterOrderByCreatedAtDesc(type, since);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/patient/{patientId}/summary")
    public ResponseEntity<RecommendationSummary> getRecommendationSummary(@PathVariable String patientId) {
        log.info("Fetching AI recommendation summary for patient: {}", patientId);
        
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        
        long recentCount = recommendationRepository.countByPatientIdAndCreatedAtAfter(patientId, last7Days);
        long monthlyCount = recommendationRepository.countByPatientIdAndCreatedAtAfter(patientId, last30Days);
        
        List<AIRecommendation> highPriorityRecommendations = recommendationRepository
                .findByPatientIdAndPriorityOrderByCreatedAtDesc(patientId, "high");
        
        List<AIRecommendation> criticalRecommendations = recommendationRepository
                .findByPatientIdAndPriorityOrderByCreatedAtDesc(patientId, "critical");
        
        RecommendationSummary summary = new RecommendationSummary(
                recentCount,
                monthlyCount,
                highPriorityRecommendations.size(),
                criticalRecommendations.size()
        );
        
        return ResponseEntity.ok(summary);
    }
    
    public record RecommendationSummary(
            long recentCount,
            long monthlyCount,
            long highPriorityCount,
            long criticalCount
    ) {}
}
