package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

  private final PatientRepository patientRepository;
  private final BillingServiceGrpcClient billingServiceGrpcClient;
  private final KafkaProducer kafkaProducer;

  // AWS SQS client for publishing events to serverless consumers
  private final Optional<software.amazon.awssdk.services.sqs.SqsClient> sqsClient;
  // Queue URL injected via application properties
  private final String queueUrl;

  public PatientService(PatientRepository patientRepository,
      BillingServiceGrpcClient billingServiceGrpcClient,
      KafkaProducer kafkaProducer,
      @Autowired(required = false) software.amazon.awssdk.services.sqs.SqsClient sqsClient,
      @org.springframework.beans.factory.annotation.Value("${aws.sqs.queue-url:}") String queueUrl) {
    this.patientRepository = patientRepository;
    this.billingServiceGrpcClient = billingServiceGrpcClient;
    this.kafkaProducer = kafkaProducer;
    this.sqsClient = Optional.ofNullable(sqsClient);
    this.queueUrl = queueUrl;
  }

  public List<PatientResponseDTO> getPatients() {
    List<Patient> patients = patientRepository.findAll();

    return patients.stream().map(PatientMapper::toDTO).toList();
  }

  public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
    if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
      throw new EmailAlreadyExistsException(
          "A patient with this email " + "already exists"
              + patientRequestDTO.getEmail());
    }

    Patient newPatient = patientRepository.save(
        PatientMapper.toModel(patientRequestDTO));

    // Try to create billing account - don't fail if billing service is unavailable
    try {
      billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),
          newPatient.getName(), newPatient.getEmail());
    } catch (Exception e) {
      System.err.println("Failed to create billing account: " + e.getMessage());
    }

    // Try to send Kafka event - don't fail if Kafka is unavailable  
    try {
      kafkaProducer.sendEvent(newPatient);
    } catch (Exception e) {
      System.err.println("Failed to send Kafka event: " + e.getMessage());
    }

    // Publish a message to AWS SQS so a Lambda function can send a welcome email.
    // The message body contains a simple JSON representation of the patient.
    try {
      if (sqsClient.isPresent() && queueUrl != null && !queueUrl.isEmpty()) {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String messageBody = mapper.writeValueAsString(new PatientCreatedMessage(newPatient.getId(), newPatient.getName(), newPatient.getEmail()));
        software.amazon.awssdk.services.sqs.model.SendMessageRequest request = software.amazon.awssdk.services.sqs.model.SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();
        sqsClient.get().sendMessage(request);
      } else {
        System.out.println("SQS client or queue URL not configured - skipping SQS message");
      }
    } catch (Exception e) {
      // Log and continue – failure to send SQS message should not block patient creation
      System.err.println("Failed to send SQS message: " + e.getMessage());
    }

    return PatientMapper.toDTO(newPatient);
  }

  public PatientResponseDTO updatePatient(UUID id,
      PatientRequestDTO patientRequestDTO) {

    Patient patient = patientRepository.findById(id).orElseThrow(
        () -> new PatientNotFoundException("Patient not found with ID: " + id));

    if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),
        id)) {
      throw new EmailAlreadyExistsException(
          "A patient with this email " + "already exists"
              + patientRequestDTO.getEmail());
    }

    patient.setName(patientRequestDTO.getName());
    patient.setAddress(patientRequestDTO.getAddress());
    patient.setEmail(patientRequestDTO.getEmail());
    patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

    Patient updatedPatient = patientRepository.save(patient);
    return PatientMapper.toDTO(updatedPatient);
  }

  public void deletePatient(UUID id) {
    patientRepository.deleteById(id);
  }

  /**
   * Internal DTO used for SQS serialization. This separate type prevents
   * accidental leakage of sensitive fields (e.g. address, date of birth) and
   * decouples the message schema from the database entity.
   */
  private record PatientCreatedMessage(UUID id, String name, String email) {
  }
}
