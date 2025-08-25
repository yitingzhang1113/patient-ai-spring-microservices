# Modified Patient Service with Auth0 and AWS Serverless Integration

This module is an enhanced version of the original `patient-service` from
Chris Blakely's Java/Spring microservices demo. It demonstrates how to:

1. **Secure endpoints using Auth0** as an OAuth 2.0 resource server.
2. **Publish an AWS SQS message when a new patient is created**, so that a
   serverless **AWS Lambda** function can send a welcome email via SES.

The rest of the patient business logic (persistence, gRPC integration with
the billing service and Kafka event publication) remains unchanged.

## Key Features

* **Auth0 Integration** – All endpoints are protected using JWTs issued
  by Auth0. Authorities are extracted from the `permissions` claim
  allowing fine‑grained access control (`create:patient`, `read:patient`, etc.).
* **AWS SQS Integration** – After successfully creating a patient, the service
  publishes a JSON message to a configured SQS queue. A record contains
  only the patient ID, name and email to avoid leaking sensitive
  information.
* **Serverless Email Notification** – A sample Lambda handler
  (`lambda/sendEmail.js`) is provided. It is triggered by SQS and uses
  AWS SES to send a welcome email to the patient’s email address.

## Setup

### 1. Configure Auth0

1. Create a new API in your Auth0 dashboard and note the **Identifier**
   (this will become the audience).
2. Update the following properties in
   [`src/main/resources/application.properties`](src/main/resources/application.properties):

   ```properties
   spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_AUTH0_DOMAIN/
   spring.security.oauth2.resourceserver.jwt.audience=YOUR_API_AUDIENCE
   ```

3. In your Auth0 API settings, define the required permissions
   (`create:patient`, `read:patient`, `update:patient`, `delete:patient`).
4. Configure your frontend or testing client to obtain access tokens with
   these permissions in the `permissions` claim.

### 2. Provision AWS Resources

1. **Create an SQS queue** in your AWS account (e.g. `patient-created-queue`).
2. **Create an AWS Lambda function** and paste the contents of
   [`lambda/sendEmail.js`](lambda/sendEmail.js) into the code editor.
3. Configure the Lambda function:
   - Set `SENDER_EMAIL` as an environment variable to an SES‑verified email
     address (e.g. `no-reply@yourdomain.com`).
   - Add an SQS trigger pointing to your queue.
   - Ensure the Lambda has IAM permissions to call `ses:SendEmail`.
4. Replace the placeholder URL in `application.properties` with your actual
   queue URL:

   ```properties
   aws.sqs.queue-url=https://sqs.<region>.amazonaws.com/<account-id>/patient-created-queue
   aws.region=<region>
   ```

### 3. Run the Service

This module is built with Maven. In the project root, run:

```bash
./mvnw spring-boot:run
```

Ensure that your environment has access to AWS credentials (via
`AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`, or an IAM role when running on
EC2/ECS) and that the Auth0 issuer and audience are configured correctly.

## API

### Create a Patient

```
POST /patients
Authorization: Bearer <access_token_with_create:patient>
Content-Type: application/json

{
  "name": "John Doe",
  "address": "123 Main St",
  "email": "john@example.com",
  "dateOfBirth": "1990-01-01"
}
```

If the request is successful:

* A patient record is saved to the database.
* A billing account is created via gRPC.
* A Kafka event is emitted as in the original implementation.
* A minimal JSON message `{ "id": ..., "name": ..., "email": ... }` is
  published to SQS, triggering the Lambda function to send a welcome email.

### Read/Update/Delete Patients

These endpoints mirror the original service but now require the
corresponding `read:patient`, `update:patient` and `delete:patient` permissions.

## Notes

* The SQS integration is deliberately non‑blocking. If the queue publish
  fails, the patient will still be created and the error is logged.
* The provided Lambda handler is an example written in Node.js. You can
  implement your own handler in Java, Python or any supported language.
* For local development without connecting to AWS, you can use
  [LocalStack](https://github.com/localstack/localstack) to emulate SQS and SES.