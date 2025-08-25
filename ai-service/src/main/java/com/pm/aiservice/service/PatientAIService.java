package com.pm.aiservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.aiservice.model.AIRecommendation;
import com.pm.aiservice.model.PatientEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientAIService {
    
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    
    public AIRecommendation processClinicalNote(PatientEvent event) {
        try {
            String prompt = createClinicalNotePrompt(event);
            String aiResponse = geminiService.getAnswer(prompt).block();
            return parseAIResponse(event, aiResponse, AIRecommendation.RecommendationType.CLINICAL_NOTE_SUMMARY);
        } catch (Exception e) {
            log.error("Error processing clinical note for patient {}: {}", event.getPatientId(), e.getMessage());
            return createDefaultRecommendation(event, AIRecommendation.RecommendationType.CLINICAL_NOTE_SUMMARY);
        }
    }
    
    public AIRecommendation processTriageAssessment(PatientEvent event) {
        try {
            String prompt = createTriagePrompt(event);
            String aiResponse = geminiService.getAnswer(prompt).block();
            return parseAIResponse(event, aiResponse, AIRecommendation.RecommendationType.TRIAGE_ASSESSMENT);
        } catch (Exception e) {
            log.error("Error processing triage assessment for patient {}: {}", event.getPatientId(), e.getMessage());
            return createDefaultRecommendation(event, AIRecommendation.RecommendationType.TRIAGE_ASSESSMENT);
        }
    }
    
    public AIRecommendation processCodingSuggestion(PatientEvent event) {
        try {
            String prompt = createCodingPrompt(event);
            String aiResponse = geminiService.getAnswer(prompt).block();
            return parseAIResponse(event, aiResponse, AIRecommendation.RecommendationType.CODING_SUGGESTION);
        } catch (Exception e) {
            log.error("Error processing coding suggestion for patient {}: {}", event.getPatientId(), e.getMessage());
            return createDefaultRecommendation(event, AIRecommendation.RecommendationType.CODING_SUGGESTION);
        }
    }
    
    private String createClinicalNotePrompt(PatientEvent event) {
        Map<String, Object> eventData = event.getEventData();
        
        return String.format("""
            As a clinical AI assistant, analyze the following patient data and provide a structured assessment:
            
            Patient ID: %s
            Event Type: %s
            Clinical Data: %s
            
            Please provide a JSON response with the following structure:
            {
              "clinicalSummary": "Brief clinical summary",
              "recommendations": ["recommendation1", "recommendation2"],
              "safetyNotes": ["safety note1", "safety note2"],
              "priority": "low|medium|high|critical",
              "analysis": {
                "clinicalSummary": "Detailed clinical assessment",
                "suggestedDiagnosisCodes": ["ICD10 codes"],
                "suggestedProcedureCodes": ["CPT codes"],
                "triagePriority": "Priority level",
                "recommendedCareLevel": "primary|urgent|emergency|telehealth",
                "confidenceScore": 0.85
              }
            }
            
            Focus on patient safety and provide evidence-based recommendations.
            """, 
            event.getPatientId(), 
            event.getEventType(), 
            eventData.toString()
        );
    }
    
    private String createTriagePrompt(PatientEvent event) {
        Map<String, Object> eventData = event.getEventData();
        
        return String.format("""
            As a triage AI assistant, assess the following patient presentation and provide triage recommendations:
            
            Patient ID: %s
            Presentation Data: %s
            
            Please provide a JSON response focusing on triage priority and care level recommendations:
            {
              "triagePriority": "ESI Level 1-5 or Priority description",
              "recommendedCareLevel": "emergency|urgent|primary|telehealth",
              "priority": "low|medium|high|critical",
              "recommendations": ["immediate actions", "follow-up care"],
              "safetyNotes": ["red flags", "contraindications"],
              "confidenceScore": 0.90
            }
            
            Consider severity, urgency, and appropriate care setting.
            """, 
            event.getPatientId(), 
            eventData.toString()
        );
    }
    
    private String createCodingPrompt(PatientEvent event) {
        Map<String, Object> eventData = event.getEventData();
        
        return String.format("""
            As a medical coding AI assistant, analyze the clinical documentation and suggest appropriate codes:
            
            Patient ID: %s
            Clinical Documentation: %s
            
            Please provide a JSON response with coding suggestions:
            {
              "suggestedDiagnosisCodes": ["ICD-10 codes with descriptions"],
              "suggestedProcedureCodes": ["CPT codes with descriptions"],
              "recommendations": ["coding guidance", "documentation suggestions"],
              "confidenceScore": 0.80,
              "priority": "medium"
            }
            
            Ensure codes are current and accurately reflect the documented care.
            """, 
            event.getPatientId(), 
            eventData.toString()
        );
    }
    
    private AIRecommendation parseAIResponse(PatientEvent event, String aiResponse, AIRecommendation.RecommendationType type) {
        try {
            // Try to extract JSON from the AI response
            String jsonString = extractJsonFromResponse(aiResponse);
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            
            AIRecommendation recommendation = new AIRecommendation();
            recommendation.setPatientId(event.getPatientId());
            recommendation.setSourceType(event.getEventType());
            recommendation.setSourceId(event.getSourceServiceId());
            recommendation.setType(type);
            recommendation.setCreatedAt(LocalDateTime.now());
            recommendation.setUpdatedAt(LocalDateTime.now());
            
            // Parse basic fields
            recommendation.setTitle(getTextValue(jsonNode, "title", "AI Assessment"));
            recommendation.setSummary(getTextValue(jsonNode, "clinicalSummary", aiResponse.substring(0, Math.min(200, aiResponse.length()))));
            recommendation.setPriority(getTextValue(jsonNode, "priority", "medium"));
            
            // Parse arrays
            recommendation.setRecommendations(getArrayValue(jsonNode, "recommendations"));
            recommendation.setSafetyNotes(getArrayValue(jsonNode, "safetyNotes"));
            
            // Parse analysis
            JsonNode analysisNode = jsonNode.path("analysis");
            if (!analysisNode.isMissingNode()) {
                AIRecommendation.AIAnalysis analysis = new AIRecommendation.AIAnalysis();
                analysis.setClinicalSummary(getTextValue(analysisNode, "clinicalSummary", ""));
                analysis.setSuggestedDiagnosisCodes(getArrayValue(analysisNode, "suggestedDiagnosisCodes"));
                analysis.setSuggestedProcedureCodes(getArrayValue(analysisNode, "suggestedProcedureCodes"));
                analysis.setTriagePriority(getTextValue(analysisNode, "triagePriority", ""));
                analysis.setRecommendedCareLevel(getTextValue(analysisNode, "recommendedCareLevel", ""));
                analysis.setConfidenceScore(analysisNode.path("confidenceScore").asDouble(0.5));
                recommendation.setAnalysis(analysis);
            }
            
            return recommendation;
            
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            return createDefaultRecommendation(event, type);
        }
    }
    
    private String extractJsonFromResponse(String response) {
        // Try to find JSON in the response
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }
        
        // If no JSON found, create a minimal JSON structure
        return String.format("""
            {
              "clinicalSummary": "%s",
              "recommendations": ["Review provided information", "Consider clinical assessment"],
              "safetyNotes": ["Follow institutional protocols"],
              "priority": "medium"
            }
            """, response.replaceAll("\"", "'"));
    }
    
    private String getTextValue(JsonNode node, String fieldName, String defaultValue) {
        return node.path(fieldName).asText(defaultValue);
    }
    
    private List<String> getArrayValue(JsonNode node, String fieldName) {
        JsonNode arrayNode = node.path(fieldName);
        if (arrayNode.isArray()) {
            return Arrays.stream(objectMapper.convertValue(arrayNode, String[].class)).toList();
        }
        return List.of();
    }
    
    private AIRecommendation createDefaultRecommendation(PatientEvent event, AIRecommendation.RecommendationType type) {
        AIRecommendation recommendation = new AIRecommendation();
        recommendation.setPatientId(event.getPatientId());
        recommendation.setSourceType(event.getEventType());
        recommendation.setSourceId(event.getSourceServiceId());
        recommendation.setType(type);
        recommendation.setTitle("Default Assessment");
        recommendation.setSummary("AI analysis temporarily unavailable. Please perform manual assessment.");
        recommendation.setRecommendations(List.of("Perform manual clinical assessment", "Follow institutional protocols"));
        recommendation.setSafetyNotes(List.of("Ensure all safety protocols are followed"));
        recommendation.setPriority("medium");
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setUpdatedAt(LocalDateTime.now());
        
        // Default analysis
        AIRecommendation.AIAnalysis analysis = new AIRecommendation.AIAnalysis();
        analysis.setClinicalSummary("Manual assessment required");
        analysis.setTriagePriority("Standard");
        analysis.setRecommendedCareLevel("primary");
        analysis.setConfidenceScore(0.0);
        recommendation.setAnalysis(analysis);
        
        return recommendation;
    }
}
