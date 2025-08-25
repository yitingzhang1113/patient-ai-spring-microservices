package com.pm.aiservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_recommendations")
public class AIRecommendation {
    @Id
    private String id;
    
    private String patientId;
    private String sourceType; // "clinical_note", "vital_signs", "patient_activity"
    private String sourceId;
    
    private RecommendationType type;
    private String title;
    private String summary;
    private List<String> recommendations;
    private List<String> safetyNotes;
    private String priority; // "low", "medium", "high", "critical"
    
    private AIAnalysis analysis;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Explicit getters and setters for compatibility
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    
    public RecommendationType getType() { return type; }
    public void setType(RecommendationType type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    
    public List<String> getSafetyNotes() { return safetyNotes; }
    public void setSafetyNotes(List<String> safetyNotes) { this.safetyNotes = safetyNotes; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public AIAnalysis getAnalysis() { return analysis; }
    public void setAnalysis(AIAnalysis analysis) { this.analysis = analysis; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIAnalysis {
        private String clinicalSummary;
        private List<String> suggestedDiagnosisCodes; // ICD-10 codes
        private List<String> suggestedProcedureCodes; // CPT codes
        private String triagePriority;
        private String recommendedCareLevel; // "primary", "urgent", "emergency", "telehealth"
        private Double confidenceScore;
        
        // Explicit getters and setters
        public String getClinicalSummary() { return clinicalSummary; }
        public void setClinicalSummary(String clinicalSummary) { this.clinicalSummary = clinicalSummary; }
        
        public List<String> getSuggestedDiagnosisCodes() { return suggestedDiagnosisCodes; }
        public void setSuggestedDiagnosisCodes(List<String> suggestedDiagnosisCodes) { this.suggestedDiagnosisCodes = suggestedDiagnosisCodes; }
        
        public List<String> getSuggestedProcedureCodes() { return suggestedProcedureCodes; }
        public void setSuggestedProcedureCodes(List<String> suggestedProcedureCodes) { this.suggestedProcedureCodes = suggestedProcedureCodes; }
        
        public String getTriagePriority() { return triagePriority; }
        public void setTriagePriority(String triagePriority) { this.triagePriority = triagePriority; }
        
        public String getRecommendedCareLevel() { return recommendedCareLevel; }
        public void setRecommendedCareLevel(String recommendedCareLevel) { this.recommendedCareLevel = recommendedCareLevel; }
        
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    }
    
    public enum RecommendationType {
        CLINICAL_NOTE_SUMMARY,
        TRIAGE_ASSESSMENT,
        CODING_SUGGESTION,
        HEALTH_RECOMMENDATION,
        RISK_ASSESSMENT
    }
}
