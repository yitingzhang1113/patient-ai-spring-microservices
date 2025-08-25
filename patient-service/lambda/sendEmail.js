const AWS = require('aws-sdk');

// Create an SES client. Region can be overridden via the AWS_REGION
// environment variable when deploying the Lambda.
const ses = new AWS.SES();

/**
 * Lambda handler that is triggered by AWS SQS. For each message, it
 * extracts the patient's id, name and email address from the JSON body and
 * sends a welcome email via AWS SES. Any failures are logged but do not
 * stop processing of other messages.
 *
 * The sending email address must be verified in SES and provided via the
 * SENDER_EMAIL environment variable when configuring the Lambda function.
 *
 * @param {Object} event The event payload from SQS
 */
exports.handler = async (event) => {
  for (const record of event.Records) {
    let message;
    try {
      message = JSON.parse(record.body);
    } catch (e) {
      console.error('Invalid message body', record.body);
      continue;
    }
    const { id, name, email } = message;
    if (!email) {
      console.warn('Missing email for patient', id);
      continue;
    }
    const params = {
      Destination: {
        ToAddresses: [email],
      },
      Message: {
        Subject: {
          Data: 'Welcome to our patient portal',
        },
        Body: {
          Text: {
            Data: `Hello ${name},\n\nYour patient account has been created successfully. Thank you for registering with us!`,
          },
        },
      },
      Source: process.env.SENDER_EMAIL,
    };
    try {
      await ses.sendEmail(params).promise();
      console.info('Sent welcome email to', email);
    } catch (err) {
      console.error('Failed to send email', err);
    }
  }
  return {};
};