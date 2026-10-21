# Stroll Threat Model

## 1. Purpose

This document identifies potential security threats to the Stroll Android application using the simplified STRIDE methodology.

STRIDE classifies threats into six categories: Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, and Elevation of Privilege.

The purpose of this model is to identify important risks, document their potential impact, and define security measures to reduce them.

## 2. Scope

This threat model covers the Stroll Android application and the following components:

- User authentication through Google Account.
- Activity tracking using Google Fit or supported device sensors.
- Local storage on the Android device.
- Firebase cloud storage and synchronization.
- Google Maps integration.
- Firebase Cloud Messaging.
- Communication between the application and external services.
- Development repositories and configuration of project services.

The model focuses on the application's data flows, trust boundaries, authentication, authorization, activity data integrity, privacy, and availability.

The model does not assess the internal security architecture of Google or Firebase. It considers the risks that arise from Stroll's use and configuration of those services.

## 3. System Components and Trust Boundaries

### 3.1 Components

The system consists of the Android user interface, application services, domain logic, local storage, Google authentication services, activity data services, Google Maps, Firebase databases, and Firebase Cloud Messaging.

### 3.2 Trust Boundaries

The main trust boundaries are:

1. Between the user and the Stroll application.
2. Between the application and Android device services.
3. Between the application and Google authentication or activity services.
4. Between the application and Firebase cloud infrastructure.
5. Between the application and Google Maps services.
6. Between the development environment and the source code repository or cloud configuration.

Data crossing these boundaries must be validated, transmitted securely, and protected by appropriate access controls.

## 4. Assets Requiring Protection

The primary assets are user accounts, authentication tokens, profile information, physical activity records, location-related information, quest progress, achievements, statistics, application settings, Firebase records, API credentials, source code, and project documentation.

The system must preserve the confidentiality and integrity of personal information while maintaining the availability of core application functions.

## 5. STRIDE Threat Analysis

| ID | STRIDE Category | Threat | Potential Impact | Mitigation Measures |
|---|---|---|---|---|
| T1 | Spoofing | An attacker attempts to impersonate a legitimate user or reuse stolen authentication credentials. | Unauthorized access to a user account and associated data. | Use supported Google authentication, validate authentication tokens, apply secure session handling, and restrict access to user-specific records. |
| T2 | Spoofing | A malicious client submits activity records while pretending they came from a trusted source. | Incorrect statistics, quest completion, and rewards. | Validate data sources where possible, retain source metadata, and avoid trusting arbitrary client-submitted activity values. |
| T3 | Tampering | An attacker modifies locally stored step counts, quest progress, or achievement values. | Incorrect rewards and unreliable statistics. | Validate activity data, recalculate progress from trusted records where possible, and avoid treating local values as authoritative cloud data without checks. |
| T4 | Tampering | An unauthorized client modifies another user's Firebase records. | Loss of data integrity and unauthorized changes to profiles or achievements. | Configure Firebase security rules, enforce user-specific authorization, and test read and write permissions. |
| T5 | Tampering | Synchronization creates duplicate activity records or overwrites newer data with older records. | Inconsistent activity history and incorrect statistics. | Use stable record identifiers, idempotent synchronization, timestamps, and conflict-resolution rules. |
| T6 | Repudiation | A user or attacker disputes a significant change because the system lacks sufficient diagnostic evidence. | Difficulty investigating suspicious activity or synchronization errors. | Maintain privacy-conscious diagnostic logs and record relevant event metadata without storing passwords or authentication tokens. |
| T7 | Information Disclosure | Authentication tokens or sensitive information are exposed through logs, source code, or insecure local storage. | Account compromise and exposure of personal information. | Protect credentials, exclude secrets from logs and repositories, and use appropriate Android secure storage mechanisms. |
| T8 | Information Disclosure | Incorrect Firebase rules allow one user to read another user's activity history or profile. | Unauthorized disclosure of personal and activity information. | Apply least-privilege database rules and test access using multiple test accounts. |
| T9 | Information Disclosure | Location or activity information is collected or transmitted beyond what the feature requires. | Privacy violations and unnecessary exposure of sensitive data. | Minimize data collection, request permissions only when needed, disclose relevant processing, and restrict retention. |
| T10 | Denial of Service | Google services, Firebase, or network connectivity become unavailable. | Authentication, maps, notifications, or synchronization may fail. | Handle service errors, preserve local activity data, use controlled retries, and provide appropriate offline functionality. |
| T11 | Denial of Service | Repeated API requests or synchronization retries exhaust service quotas or battery resources. | Degraded performance, excessive battery consumption, or service limitations. | Use request limits, efficient background work, retry backoff, and appropriate synchronization intervals. |
| T12 | Elevation of Privilege | A user accesses administrative functions or changes application data beyond their permissions. | Unauthorized access to other users' data or privileged operations. | Enforce authorization on the server side, restrict administrative permissions, and never rely solely on hiding interface controls. |
| T13 | Elevation of Privilege | An insecure configuration or compromised development credential provides excessive access to Firebase or the source repository. | Exposure or modification of project data and code. | Apply least privilege, protect development credentials, review repository access, and rotate exposed secrets. |
| T14 | Tampering | A malicious or compromised device provides inaccurate sensor or activity information. | Artificially inflated steps and unfair quest rewards. | Apply plausibility checks and anomaly detection where feasible. Document that client-side checks cannot completely prevent device-level manipulation. |
| T15 | Information Disclosure | Push notification content exposes sensitive activity or account details on a device's lock screen. | Unintended disclosure to people near the user. | Keep notification content minimal and avoid including sensitive personal details. |

## 6. Risk Prioritization

The following priorities are preliminary and should be confirmed through testing and implementation review.

| Threat ID | Preliminary Priority | Reason |
|---|---|---|
| T1 | High | Account impersonation may expose private user information. |
| T4 | High | Unauthorized cloud writes can affect multiple users or corrupt stored data. |
| T7 | High | Exposed credentials can enable account or service compromise. |
| T8 | High | Incorrect database rules may disclose personal activity records. |
| T12 | High | Authorization failures may allow access to privileged functionality. |
| T13 | High | Compromised development credentials may affect the entire project infrastructure. |
| T3 | Medium | Manipulated activity data may affect statistics and rewards. |
| T5 | Medium | Synchronization errors may corrupt or duplicate activity records. |
| T9 | High | Unnecessary location or activity collection may create significant privacy risks. |
| T10 | Medium | External service failures can interrupt application functionality. |
| T11 | Medium | Excessive requests can degrade performance and exhaust quotas. |
| T14 | Medium | Device-level manipulation can undermine reward integrity. |
| T15 | Low to Medium | Notification content may disclose information depending on device settings. |
| T2 | Medium | Untrusted activity submissions may result in fraudulent progress. |
| T6 | Low to Medium | Insufficient diagnostic evidence can complicate investigations. |

These priorities are qualitative estimates rather than results of a completed security audit. They should be revised based on the actual architecture, exposed interfaces, data sensitivity, and likelihood of exploitation.

## 7. Security Requirements

The following requirements should be addressed before a public release:

- Authentication must use supported and secure mechanisms.
- Firebase rules must enforce user-specific access restrictions.
- Secrets and authentication tokens must not be exposed in source code or logs.
- Personal information must be transmitted over secure connections.
- Local data and synchronization records must be protected against unauthorized modification where feasible.
- Input and activity data must be validated before being used to award rewards.
- Synchronization must handle duplicates, failures, and conflicting updates safely.
- The application must request only necessary permissions.
- Errors from external services must not expose sensitive information.
- Dependencies and cloud configurations must be reviewed for security issues.
- Users must receive clear information about relevant data collection and processing.

## 8. Security Verification Plan

The following tests should be performed:

1. Authenticate using valid and invalid authentication states and verify correct access behavior.
2. Attempt to read or modify another user's Firebase records using a separate test account.
3. Verify that unauthenticated clients cannot access protected data.
4. Inspect logs and repository history for exposed credentials and sensitive information.
5. Test synchronization with duplicate records, interrupted connectivity, and conflicting updates.
6. Verify that activity data is validated before quest progress and rewards are updated.
7. Test behavior when Google APIs and Firebase are unavailable.
8. Review Android permissions and ensure that sensitive data is collected only when needed.
9. Verify that notification messages do not unnecessarily expose personal information.
10. Review third-party dependencies and service configuration for known security weaknesses.

## 9. Residual Risks

Some risks cannot be completely eliminated. A compromised device may provide manipulated sensor data, external services may become unavailable, and users may operate devices with outdated software or unsafe configurations.

The project should reduce these risks through validation, access control, secure configuration, local data preservation, testing, and clear user communication.

## 10. Review and Maintenance

The threat model should be updated when new features are introduced, data flows change, external services are added, or security testing identifies new risks.

The Analyst maintains the threat descriptions, the Team Lead reviews technical mitigations, programmers implement controls, the Tester verifies them, and the Project Manager coordinates review and resolution.

