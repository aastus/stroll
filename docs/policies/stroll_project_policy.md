# Project Policy — Stroll

| Title | Project Policy — Stroll |
| Version | 0.1.0 |
| Status | Draft |
| Classification | Internal |
| Last Updated | 2026-09-20 |
| Owner | Liza Tiukh, Anastasiia Rybchynska |

> This policy defines the mandatory requirements for the code and architecture of `Stroll`—an Android mobile app designed to motivate physical activity through steps, routes, quests, achievements, and statistics. Key requirements: strictly typed OOP, SOLID, clean code, separation of layers, isolation of business logic from the Android Framework and external services, and testing of business rules.
> The project currently supports Android 8.0+ and portrait orientation. Support for iOS, Apple Health, and smartwatches is considered a future extension and must not infiltrate the current Android implementation due to strict Domain dependencies.

## Policy Change Rules
- **No incidental changes** — the policy is not changed as a side effect of another task.
- **Mandatory approval** — changes must be approved by one of the project owner.
- **Separate commit + version increment** — policy changes are made in a separate commit prefixed with `[POLICY]` and accompanied by a version update.
- Policy changes must not be used to justify violations of already established architectural boundaries.

## Table of Contents
1. Scope
2. Technologies
3. Fundamental Principles
4. Architecture and Structure
5. Domain Event Policy
6. Naming Policy
7. Code Documentation Policy
8. Exception Policy
9. NULL-less Policy
10. Policy for Working with External Services
11. Data Storage and Synchronization Policy
12. Testing Policy
13. Git Workflow
14. Localization
15. Anti-Patterns

## Scope
This policy applies to:
- Kotlin code for the mobile application;
- Domain, Application, Infrastructure, and Presentation layers;
- local storage;
- integrations with Google Fit, Google Maps API, Firebase, and the push notification service;
- tests;
- documentation describing the current architecture and business logic.
This policy does not define the internal implementation of third-party services.

## Technologies
- **Kotlin** — primary development language.
- **Android Studio** — development environment.
- **Android 8.0+ (API 26+)** — minimum target version.
- **Google Fit API** — retrieving physical activity data on Android.
- **Google Maps API** — maps, points of interest, and routes.
- **Firebase** — remote data storage and synchronization.
- **Push notification service** — reminders and system notifications.
- **Google Analytics / Firebase Analytics** — product analytics.
- **Room or an equivalent local persistence layer** — local storage.
- **JUnit** and Android/Kotlin testing tools — testing.
- **Git** — version control.
A specific library may be replaced with an equivalent one only after verifying its compatibility with this policy.

## Fundamental Principles
- **Separation of Layers.** Presentation, Application, Domain, and Infrastructure are kept separate.
- **DDD for Business Logic.** Quests, progress, activity, achievements, and levels are modeled as domain concepts.
- **Third-party logic remains outside the project.** Stroll does not duplicate the business rules of Google Fit, Google Maps, Firebase, or Android.
- **Dependency inversion.** The Domain layer does not depend on the Android Framework, Google Fit, Google Maps, or Firebase SDK.
- **Strictly typed OOP, SOLID, clean code.**
- **Offline-first for local activity.** Core activity data should not be lost due to a temporary lack of internet connectivity.
- **Data security.** Credentials, tokens, and other secrets are not stored in plain text.
- **Permission minimization.** The app requests only the permissions necessary for its declared functionality.
- **The framework does not intrude into the Domain.** Android lifecycle, Context, Activity, Fragment, ViewModel, and SDK types are not used in pure domain entities.
- **Policy integrity.** Identified discrepancies are corrected rather than circumvented with local exceptions without a documented solution.

## Architecture and Structure

Recommended project structure:

```text
app/
└── src/
    ├── main/
    │   ├── java/.../
    │   │   ├── ui/
    │   │   ├── service/
    │   │   ├── data/
    │   └── res/
    └── test/
```

The project is divided into three main logical areas:

```text
ui → service → data
       ↑
    contracts
```

### UI

The `ui/` package contains the presentation layer of the application.

It includes:

* Activities / Fragments / Compose UI;
* ViewModels;
* navigation;
* permission handling;
* user input processing;
* status and error display;
* localized UI resources.

The UI layer should not contain business rules such as quest completion, reward calculation, or progress validation. It communicates with the `service/` layer to execute application operations.

### Service

The `service/` package contains the application's business and application logic.

It includes:

* use cases;
* domain entities;
* value objects;
* enums;
* business policies;
* domain services;
* domain exceptions;
* application DTOs;
* contracts/interfaces for external dependencies.

Business rules must be independent of Android framework classes and external SDKs. For example, the rule determining whether a quest can be accepted or completed belongs in this layer rather than in an Activity, Fragment, or Firebase implementation.

The `service/` layer should not directly depend on:

* Android UI classes;
* Google Fit SDK;
* Google Maps SDK;
* Firebase SDK;
* database implementation classes;
* HTTP clients.

External dependencies should be accessed through contracts/interfaces.

### Data

The `data/` package contains infrastructure and external-system implementations.

It includes:

* database and persistence implementations;
* Google Fit integration;
* Google Maps integration;
* Firebase integration;
* notification implementations;
* analytics;
* network clients;
* implementations of contracts defined in the `service/` layer;
* data mapping between external data and application/domain models.

The `data/` layer is responsible for technical details and communication with external systems. These implementation details should not be exposed to the UI layer.

### Dependency Direction

The main dependency direction is:

```text
ui → service → data
```

The `service/` layer defines contracts for required external operations, while the `data/` layer implements these contracts. This keeps business logic independent of specific technologies such as Firebase, Google Fit, Google Maps, or a particular database.

For example:

```text
service/
└── contract/
    └── StepRepository

data/
└── googlefit/
    └── GoogleFitStepRepository
```

`StepRepository` defines what the application needs, while `GoogleFitStepRepository` provides the concrete implementation using Google Fit.

The `res/` directory contains Android resources such as layouts, strings, colors, themes, drawables, and other localized resources.

## Domain Event Policy

Each Domain Event must contain:
- a unique identifier;
- the source entity’s identifier;
- the creation time;
- the event type;
- the schema version;
- a typed payload.

For Stroll, the following are used, in particular:
- `ActivityRecordedDE`;
- `QuestAcceptedDE`;
- `QuestProgressUpdatedDE`;
- `QuestCompletedDE`;
- `AchievementUnlockedDE`;
- `UserLevelChangedDE`.

Events must not contain:
- an access token;
- a refresh token;
- personal secrets;
- raw sensor data;
- full responses from external APIs.

The domain creates an event in an internal queue. The Application saves the state change, after which the Infrastructure can forward the event to the appropriate handlers.

## Naming Conventions

- Classes, interfaces, enums — `PascalCase`.
- Methods, variables, parameters — `camelCase`.
- Constants — `UPPER_SNAKE_CASE`.
- Packages — `lowercase`.
- A Kotlin file corresponds to a primary type or a logical group of types.
- Interfaces — the `I` prefix is not required in Kotlin; names such as `QuestRepository` and `RouteProvider` are preferred.
- Value Object — the `VO` suffix, if it conforms to the agreed-upon Domain structure.
- Domain Service — the `Service` suffix.
- Domain Policy — the `DPolicy` suffix.
- Domain Event — the `DE` suffix.
- Use Case — the `UC` suffix.
- Application Command — the `AC` suffix.
- Application Query — the `AQ` suffix.
- DTO — suffix `DTO`.
- Exception — suffix `Exception`.

Examples:
- `Quest`;
- `QuestProgressVO`;
- `CreateRouteQuestDPolicy`;
- `CalculateQuestProgressService`;
- `QuestCompletedDE`;
- `AcceptQuestUC`;
- `CreateRouteQuestAC`;
- `GetActiveQuestAQ`.

## Code Documentation Policy

For every public class and method, where necessary for understanding the contract, there must be documentation describing:
- purpose;
- constraints;
- parameters;
- return value;
- exceptions;
- business invariants for Entities and Value Objects.

Documentation should not simply paraphrase the code.

For domain Entities, key invariants must be documented.

## Exception Policy

All Domain and Application errors must have typed internal exceptions.

Examples:
- `QuestNotFoundException`;
- `InvalidQuestTransitionException`;
- `InvalidRouteException`;
- `InvalidActivityRecordException`;
- `AchievementAlreadyUnlockedException`;
- `ExternalServiceException`.

Raw exceptions from Google Fit, Google Maps, Firebase, or network libraries must not extend beyond the Infrastructure layer.

Infrastructure converts an external error into an internal, typed exception with a safe message.

## NULL-less Policy

In the Domain, it is recommended to minimize the use of `null` as a logical state.

Preference is given to:
- nullable types only where the absence of a value is a natural part of the data;
- sealed interfaces;
- dedicated Value Objects;
- empty collections instead of `null`;
- enums for states.

For example, the absence of a route in a `DRAFT` quest should be part of a valid creation state, but after transitioning to `AVAILABLE`, the route quest must have both waypoints and a calculated distance.


## Policy for Working with External Services

### Google Fit

- Google Fit is used exclusively through `StepActivityProvider`.
- The domain is not aware of the Google Fit API.
- The received data is normalized to `ActivityRecord`.
- Resynchronization should not duplicate the activity.

### Google Maps API

- Google Maps is used only through `RouteProvider`.
- The domain works with `GeoPointVO` and `DistanceVO`.
- The API key is not stored in the source code.
- Network and API errors are converted to internal Infrastructure/Application errors.
- A quest is not marked as complete without a valid route length.

### Firebase

- Firebase is used via Infrastructure.
- The Domain is independent of the Firebase SDK.
- Synchronization must not overwrite local data due to a temporary network outage.
- Synchronization conflicts must have a defined resolution rule prior to implementing multi-device sync functionality.

### Push Notifications

- The Domain defines an event or fact that may require a reminder.
- Infrastructure is responsible for the actual delivery of push notifications.
- The notification token is technical data belonging to Infrastructure and is not passed to the Domain.

## Data Storage and Synchronization Policy

- User activity is stored locally regardless of whether an account exists.
- For authorized users, data is synchronized with Firebase.
- The local database is the source of offline data available on the device.
- Activity records must have a unique identifier for deduplication.
- Synchronization operations must be idempotent.
- Data must not be deleted or overwritten solely due to a temporary network error.
- Quest status and earned achievements must not be lost when the app is restarted.
- Data required for offline operation must not depend on constant access to an external API.

## Permissions and Privacy Policy

- Access to physical activity and geolocation is requested only when necessary.
- The user must receive a clear explanation of the purpose of the permission.
- Geolocation data is used for route-related features.
- The collection of data not required for the operation of features must be minimized.
- API secrets and tokens are not committed to Git.
- Configuration secrets are passed via a secure environment/build configuration mechanism.
- Analytics events must not contain sensitive personal data.

## Testing Policy

### Tools

The foundation is Kotlin/JUnit and Android testing tools.

External dependencies are replaced with fake/mock implementations. Domain tests do not make real network calls.

### Priority

The following must be tested:
- Entity invariants;
- Value Objects;
- Quest state transitions;
- Policies;
- Progress calculation;
- quest completion;
- achievement tracking;
- level changes;
- activity deduplication;
- offline application scenarios;
- external integration errors.

Positive scenarios must be supplemented with negative ones:
- zero or negative target;
- negative steps;
- incorrect coordinates;
- re-completing a quest;
- re-earning an achievement;
- duplicate activity;
- inaccessible route;
- no network connection.

### Test Distribution

- **Domain:** unit tests without Android.
- **Application:** unit tests for use cases with mock/fake dependencies.
- **Infrastructure:** unit and integration tests with mocked external services.
- **Presentation:** UI/instrumentation tests for critical user scenarios.

Real Google Fit, Google Maps, and Firebase calls are not used in regular unit tests.

## Git Workflow

### Branches

| Branch | Purpose |
|---|---|
| `main` | Stable version of the project |
| `develop` | Current integration development |
| `feature/*` | Individual features |
| `fix/*` | Fixes |
| `docs/*` | Documentation and policies |

### Commit Messages

```text
[type] brief summary
```

Recommended types:
- `FEAT` — new functionality;
- `FIX` — bug fixes;
- `REFACTOR` — structural changes without altering behavior;
- `TEST` — tests;
- `DOCS` — documentation;
- `POLICY` — policy change;
- `BUILD` — build configuration.

### CI Checks

The following must pass before a merge:
- project build;
- Kotlin lint/static analysis;
- unit tests;
- dependency check;
- verification that there are no secrets in the repository.

## Localization

Supported languages:
- Ukrainian — primary;
- English — secondary.

All UI strings are stored in localization resources, not in Kotlin code.

Domain entities must not contain hard-coded UI text.

The admin panel uses English.

## Anti-patterns

| # | Anti-pattern | Description |
|---|---|---|
| 1 | Android in Domain | `Activity`, `Context`, `View`, `SensorManager`, or SDK types are used in Domain. |
| 2 | Google Fit in Domain | The domain directly imports Google Fit. |
| 3 | Google Maps in Domain | The domain directly depends on the Maps SDK or HTTP API. |
| 4 | Firebase in the Domain | Business logic interacts directly with the Firebase SDK. |
| 5 | Fat ViewModel | The ViewModel contains rules for completing quests or awarding rewards. |
| 6 | Fat Application | Business rules are implemented within the Use Case instead of the Domain. |
| 7 | Loss of Offline Data | A temporary loss of internet connection results in the loss of user activity. |
| 8 | Duplicate Activity | A single physical activity is counted multiple times due to synchronization. |
| 9 | Secrets in Git | API keys, tokens, or credentials are stored in the source code. |
| 10 | Real APIs in unit tests | Unit tests make network calls to Google/Firebase. |
| 11 | Hardcoded UI strings | UI text is hardcoded directly in the Kotlin code. |
| 12 | God Module | A single class is responsible for steps, maps, Firebase, quests, UI, and notifications all at once. |
| 13 | Invariant violations | An entity can be put into an invalid state due to public mutable state. |
| 14 | Undefined synchronization | Data is overwritten without any rules for deduplication or conflict resolution. |
