package com.pm.aiservice.repository;

import com.pm.aiservice.model.AIRecommendation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIRecommendationRepository extends MongoRepository<AIRecommendation, String> {
    
    List<AIRecommendation> findByPatientIdOrderByCreatedAtDesc(String patientId);
    
    List<AIRecommendation> findByPatientIdAndTypeOrderByCreatedAtDesc(String patientId, AIRecommendation.RecommendationType type);
    
    List<AIRecommendation> findByPatientIdAndPriorityOrderByCreatedAtDesc(String patientId, String priority);
    
    @Query("{'patientId': ?0, 'createdAt': {$gte: ?1}}")
    List<AIRecommendation> findByPatientIdAndCreatedAtAfter(String patientId, LocalDateTime since);
    
    List<AIRecommendation> findByTypeAndCreatedAtAfterOrderByCreatedAtDesc(AIRecommendation.RecommendationType type, LocalDateTime since);
    
    long countByPatientIdAndCreatedAtAfter(String patientId, LocalDateTime since);
}
