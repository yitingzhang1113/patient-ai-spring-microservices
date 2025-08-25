package com.pm.aiservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String PATIENT_EVENTS_EXCHANGE = "patient.events.exchange";
    public static final String PATIENT_EVENTS_QUEUE = "patient.events.queue";
    public static final String CLINICAL_NOTES_QUEUE = "clinical.notes.queue";
    public static final String TRIAGE_ASSESSMENTS_QUEUE = "triage.assessments.queue";
    
    @Bean
    public TopicExchange patientEventsExchange() {
        return new TopicExchange(PATIENT_EVENTS_EXCHANGE);
    }
    
    @Bean
    public Queue patientEventsQueue() {
        return new Queue(PATIENT_EVENTS_QUEUE, true);
    }
    
    @Bean
    public Queue clinicalNotesQueue() {
        return new Queue(CLINICAL_NOTES_QUEUE, true);
    }
    
    @Bean
    public Queue triageAssessmentsQueue() {
        return new Queue(TRIAGE_ASSESSMENTS_QUEUE, true);
    }
    
    @Bean
    public Binding patientEventsBinding() {
        return BindingBuilder
                .bind(patientEventsQueue())
                .to(patientEventsExchange())
                .with("patient.*");
    }
    
    @Bean
    public Binding clinicalNotesBinding() {
        return BindingBuilder
                .bind(clinicalNotesQueue())
                .to(patientEventsExchange())
                .with("patient.note.*");
    }
    
    @Bean
    public Binding triageAssessmentsBinding() {
        return BindingBuilder
                .bind(triageAssessmentsQueue())
                .to(patientEventsExchange())
                .with("patient.triage.*");
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
