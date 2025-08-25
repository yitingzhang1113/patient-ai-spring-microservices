package com.pm.aiservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.aiservice.model.AIRecommendation;
import com.pm.aiservice.model.PatientEvent;
import com.pm.aiservice.repository.AIRecommendationRepository;
import com.pm.aiservice.service.PatientAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatientEventListener {
    
    private final PatientAIService patientAIService;
    private final AIRecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper;
    
    @RabbitListener(queues = "patient.events.queue")
    public void handlePatientEvent(String message) {
        try {
            PatientEvent event = objectMapper.readValue(message, PatientEvent.class);
            log.info("Processing patient event: {} for patient: {}", event.getEventType(), event.getPatientId());
            
            AIRecommendation recommendation = processEventBasedOnType(event);
            
            if (recommendation != null) {
                AIRecommendation savedRecommendation = recommendationRepository.save(recommendation);
                log.info("Saved AI recommendation {} for patient {}", 
                    savedRecommendation.getId(), savedRecommendation.getPatientId());
            }
            
        } catch (Exception e) {
            log.error("Error processing patient event: {}", e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = "clinical.notes.queue")
    public void handleClinicalNote(String message) {
        try {
            PatientEvent event = objectMapper.readValue(message, PatientEvent.class);
            log.info("Processing clinical note event for patient: {}", event.getPatientId());
            
            AIRecommendation recommendation = patientAIService.processClinicalNote(event);
            AIRecommendation savedRecommendation = recommendationRepository.save(recommendation);
            
            log.info("Saved clinical note AI analysis {} for patient {}", 
                savedRecommendation.getId(), savedRecommendation.getPatientId());
                
        } catch (Exception e) {
            log.error("Error processing clinical note event: {}", e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = "triage.assessments.queue")
    public void handleTriageAssessment(String message) {
        try {
            PatientEvent event = objectMapper.readValue(message, PatientEvent.class);
            log.info("Processing triage assessment event for patient: {}", event.getPatientId());
            
            AIRecommendation recommendation = patientAIService.processTriageAssessment(event);
            AIRecommendation savedRecommendation = recommendationRepository.save(recommendation);
            
            log.info("Saved triage AI assessment {} for patient {}", 
                savedRecommendation.getId(), savedRecommendation.getPatientId());
                
        } catch (Exception e) {
            log.error("Error processing triage assessment event: {}", e.getMessage(), e);
        }
    }
    
    private AIRecommendation processEventBasedOnType(PatientEvent event) {
        return switch (event.getEventType()) {
            case "patient.note.created", "patient.note.updated" -> 
                patientAIService.processClinicalNote(event);
            case "patient.vitals.updated", "patient.symptoms.reported" -> 
                patientAIService.processTriageAssessment(event);
            case "patient.visit.completed" -> 
                patientAIService.processCodingSuggestion(event);
            default -> {
                log.warn("Unhandled event type: {}", event.getEventType());
                yield null;
            }
        };
    }
}
