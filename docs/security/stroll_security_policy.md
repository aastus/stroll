# Stroll Security Policy

## 1. Purpose

This Security Policy defines the security requirements, rules, and procedures for developing, testing, maintaining, and operating the Stroll mobile application. Its purpose is to protect user accounts, personal information, physical activity records, application resources, and communication with external services.

## 2. Scope

This policy applies to the Stroll Android application, its source code, local data storage, Firebase infrastructure, Google Account authentication, Google Fit integration, Google Maps integration, Firebase Cloud Messaging, development tools, and project documentation.

It applies to all project team members involved in development, analysis, testing, and maintenance.

## 3. Security Objectives

The main security objectives are:

- Confidentiality: prevent unauthorized access to personal information and application data.
- Integrity: prevent unauthorized modification of activity records, quest progress, achievements, and user profiles.
- Availability: maintain application functionality and preserve locally collected data during network failures.
- Authentication: ensure that only authenticated users can access their accounts and associated cloud data.
- Accountability: maintain sufficient diagnostic information to investigate errors and security incidents without unnecessarily collecting sensitive information.

## 4. Roles and Responsibilities

The Project Manager coordinates security activities and ensures that security requirements are included in project planning.

The Team Lead reviews architectural decisions, source code, and security-related changes.

Programmers implement secure authentication, data storage, access control, API integration, and error handling.

The Analyst documents security requirements, identifies potential risks, and ensures that data flows and system boundaries are described.

The Tester verifies authentication, authorization, data validation, offline behavior, and protection against common security vulnerabilities.

All team members must report suspected vulnerabilities, exposed credentials, unauthorized access, and other security incidents.

## 5. Authentication and Access Control

Stroll uses Google Account authentication through OAuth 2.0 and the applicable Google authentication libraries.

The application must use supported authentication mechanisms and must not collect or store Google account passwords directly.

Authentication tokens and other credentials must be handled according to the relevant provider's security recommendations. Tokens must not be written to application logs or committed to the source code repository.

Firebase security rules must restrict access to user-specific records. Each authenticated user must only be able to read and modify data they are authorized to access.

Administrative access to Firebase, Google Cloud, source repositories, and other project services must be granted only to authorized team members.

## 6. Data Protection

Stroll processes user profile information, physical activity records, quest progress, achievements, settings, and potentially location-related information.

The application must collect only the data necessary for its documented functionality.

Data transmitted between the application and external services must use secure communication protocols, such as HTTPS.

Sensitive local information must be stored using appropriate Android security mechanisms. Authentication credentials and tokens must not be stored in plain-text files or ordinary application logs.

Cloud databases must use appropriate access controls and security rules. Production credentials and service secrets must not be embedded in publicly accessible source code.

## 7. Activity Data Integrity

Activity data received from Google Fit or supported device sensors must be validated before it is used to update statistics, quest progress, or achievements.

The application should detect duplicate records, invalid values, unexpected timestamps, and inconsistent synchronization states.

The system must not treat arbitrary client-submitted values as automatically trustworthy. Where supported by the data source, activity records should retain sufficient source and timestamp information for validation.

The application should minimize opportunities for artificial step inflation. However, complete prevention of manipulated device or sensor data cannot be guaranteed by client-side validation alone.

## 8. Offline Storage and Synchronization

Stroll must preserve collected activity data when an Internet connection is unavailable.

Locally stored records must be associated with the correct user and tracked using a synchronization status.

When connectivity returns, the application must synchronize eligible records with Firebase.

Synchronization must be designed to avoid unintended duplication, overwriting newer data with older data, or transferring one user's records to another user's account.

Failed synchronization attempts must not result in the permanent deletion of unsynchronized data. The application should retry synchronization according to an appropriate retry policy.

## 9. External Services and API Security

Stroll integrates with Google Account authentication, Google Fit, Google Maps, Firebase, and Firebase Cloud Messaging.

Only the permissions and API scopes required for application functionality should be requested.

API keys must be restricted according to the capabilities of the relevant service. Secrets must not be exposed in the source repository.

The application must handle unavailable services, authentication failures, API errors, and rate limits without exposing sensitive implementation details to users.

Third-party SDKs and dependencies should be kept reasonably up to date, and security-relevant updates should be reviewed before integration.

## 10. Development and Repository Security

Source code must be maintained in a version control system with access restricted to authorized contributors.

Changes to authentication, database rules, data synchronization, and permissions should receive additional review.

The team must not commit passwords, private keys, access tokens, production credentials, or real user data to the repository.

Development and testing should use test accounts and synthetic data wherever possible.

Dependencies should be obtained from trusted sources and reviewed for known security issues when practical.

## 11. Logging and Error Handling

Logs may contain technical information necessary for debugging, but must not contain passwords, authentication tokens, or unnecessary personal information.

Error messages displayed to users should provide useful guidance without exposing database structures, credentials, internal paths, or detailed server errors.

Security-relevant failures should be investigated and documented when appropriate.

## 12. Security Testing

Security testing should cover:

- Authentication and logout behavior.
- Access restrictions for user-specific data.
- Firebase database security rules.
- Secure handling of credentials and tokens.
- Input validation and activity data integrity.
- Offline storage and synchronization.
- Network and external API failures.
- Application permissions and sensitive information in logs.
- Dependency and configuration vulnerabilities.

Identified vulnerabilities must be prioritized according to their severity and corrected before release when they present an unacceptable risk.

## 13. Incident Response

When a security incident is suspected, the team must assess the affected component and the potential impact on user data.

The team should restrict compromised access, revoke or rotate exposed credentials, correct the underlying vulnerability, and verify the effectiveness of the corrective actions.

If personal data may have been exposed, the team must assess the incident and follow applicable legal and service-provider requirements.

Incidents and remediation actions should be documented without including unnecessary sensitive information.

## 14. Backup and Recovery

Important project documentation and source code must be maintained in an appropriate version control or backup system.

Cloud data recovery procedures should be considered for the selected Firebase database configuration.

The application must preserve unsynchronized local activity records where possible. Recovery procedures should be tested to reduce the risk of data loss.

## 15. Policy Review

This policy should be reviewed when significant application features, data flows, external services, or security requirements change.

The Project Manager coordinates reviews, and the Team Lead and relevant developers contribute to technical updates.

