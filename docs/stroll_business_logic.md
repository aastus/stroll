# Stroll BUSINESS LOGIC

## Purpose

`Stroll` is the bounded context of a mobile app designed to motivate users to engage in daily physical activity through steps, routes, quests, achievements, and statistics.

`Stroll` features:
- tracking of the user’s physical activity and step count;
- creation and completion of standard and customizable quests;
- building quests based on the actual distance between two geographic points;
- tracking the user’s progress in the current quest;
- a system of rewards, levels, and achievements;
- saving a history of completed quests and activity statistics;
- local data storage for users without an account and remote synchronization for logged-in users;
- generating data for push notifications about activity.

**NOT here:**
- actually retrieving raw data from smartphone sensors — `Infrastructure`;
- retrieving Google Fit data — `Infrastructure`;
- working with the Google Maps API and calculating routes via an external service — `Infrastructure`;
- authentication and account management — `Infrastructure` / `Application`;
- physical delivery of push notifications — `Infrastructure`;
- collection of technical analytics from Google Analytics / Firebase Analytics — `Infrastructure`;
- Android app UI and screen navigation — `Presentation`.

## Entities

| Entity | Basic Fields | Description | Invariants |
|---|---|---|---|
| `User` *(Aggregate Root)* | `uuid`, `displayName`, `level`, `totalSteps`, `totalDistance`, `createdAt` | Represents a local or authorized user and their overall progress. | `uuid` is invariant. `level >= 1`. `totalSteps >= 0`. `totalDistance >= 0`. Total counters cannot decrease as a result of normal activity additions. |
| `Quest` *(Aggregate Root)* | `uuid`, `title`, `description`, `type`, `status`, `targetDistance`, `targetSteps`, `startPoint`, `endPoint`, `routeDistance`, `progress`, `rewardRef` | A business task with completion conditions and a reward. It can be standard or user-created. | `uuid` is immutable. A quest has a positive target value. For a route quest, the start and end points and a positive route distance are specified. Progress cannot be negative or exceed 100%. A completed quest does not return to the active state. |
| `QuestProgress` *(internal Entity — owned by `Quest`)* | `currentSteps`, `currentDistance`, `startedAt`, `completedAt` | The current execution status of a specific quest. | `currentSteps >= 0`. `currentDistance >= 0`. `completedAt` is set only after the goal is achieved. Progress cannot decrease due to the correct receipt of new activity data. |
| `Achievement` *(Entity)* | `uuid`, `code`, `title`, `description`, `condition`, `reward`, `unlockedAt` | A reward for fulfilling a specific activity condition or completing a quest. | `code` is unique. An achievement can be earned no more than once per user. `unlockedAt` is set once. |
| `ActivityRecord` *(Entity)* | `uuid`, `date`, `steps`, `distance`, `source` | A normalized physical activity record received from a device or an external service. | `uuid` is immutable. `steps >= 0`. `distance >= 0`. The activity source must be specified. Resynchronization must not create a duplicate of the same external record. |

## Quest Lifecycle

A new quest is created with the status `AVAILABLE` if it is available to the user, or `DRAFT` for a customizable quest pending confirmation of parameters.

Allowed transitions:

| From | User-initiated | System-initiated |
|---|---|---|
| `DRAFT` | → `AVAILABLE`, `CANCELLED` | — |
| `AVAILABLE` | → `ACTIVE` | — |
| `ACTIVE` | → `CANCELLED` | → `COMPLETED` |
| `COMPLETED` | — (terminal) | — (terminal) |
| `CANCELLED` | — (terminal) | — (terminal) |

Rules:
- `DRAFT` can only be activated after all parameters have been successfully verified.
- `ACTIVE` is set when the user accepts the quest.
- `COMPLETED` is set by the system only after the target metric has been achieved.
- Once `COMPLETED`, the quest cannot be canceled or reactivated.
- A new quest can be offered after the previous one is completed.

## Quest Types

| Type | Description | Completion Condition |
|---|---|---|
| `STEP_TARGET` | A quest to take a specified number of steps. | Current number of steps >= target. |
| `ROUTE` | A quest to cover the actual distance between two points. | Accumulated distance >= route length. |
| `CUSTOM` | A user-configurable quest. | A user-defined, validated goal is achieved. |

For the `ROUTE` quest:
- `startPoint` and `endPoint` are defined by the user;
- `routeDistance` is obtained via an external mapping service;
- the domain is independent of any specific implementation of the Google Maps API;
- if the external service does not return the correct distance, the quest cannot be confirmed as ready for execution.

## Value Objects

| Value Object | Description | Invariants |
|---|---|---|
| `UserUuidVO` | User identifier. | Immutable, valid UUID. |
| `QuestUuidVO` | Quest identifier. | Immutable, valid UUID. |
| `AchievementUuidVO` | Achievement identifier. | Immutable, valid UUID. |
| `GeoPointVO` | Geographical point on the route. | Contains valid latitude and longitude coordinates. |
| `DistanceVO` | Distance in normalized units. | The value cannot be negative. |
| `StepCountVO` | Number of steps. | An integer value, not less than 0. |
| `QuestProgressVO` | Quest progress. | A value in the range `0..100%`. |
| `QuestTitleVO` | Quest title. | Not empty after normalization; length is limited by UI/domain policy. |

## Domain Policies

| Domain Policy | Description |
|---|---|
| `AcceptQuestDPolicy` | Checks whether a quest is available for the user to accept. |
| `CompleteQuestDPolicy` | Checks whether the target metric has been achieved and allows an active quest to be completed. |
| `CreateRouteQuestDPolicy` | Checks for the presence of two valid waypoints and a positive route length. |
| `UpdateQuestProgressDPolicy` | Does not allow negative progress or incorrect increases in activity metrics. |
| `UnlockAchievementDPolicy` | Checks the achievement condition and ensures that a specific achievement can be earned only once. |
| `ApplyActivityDPolicy` | Verifies that the activity record has valid, non-negative values and can be applied to progress. |

## Domain Services

| Domain Service | Operation |
|---|---|
| `CalculateQuestProgressService` | Calculates the progress of an active quest based on accumulated steps or distance. |
| `ApplyActivityToQuestService` | Applies a new activity record to the active quest and determines whether the goal has been achieved. |
| `EvaluateAchievementsService` | Checks the conditions for available achievements after an activity change or quest completion. |
| `CalculateUserLevelService` | Calculates the user’s level based on accumulated progress. |
| `CalculateDistanceProgressService` | Normalizes the activity distance and determines its contribution to a route-based quest. |

External services are accessible only through `OutsourceContract`.

## Outsource Contracts

| Contract | Purpose |
|---|---|
| `IStepActivityProvider` | Retrieves step count and other physical activity data from Google Fit or device sensors. |
| `IRouteProvider` | Retrieves the route length and, if necessary, the route geometry between two points. |
| `IUserDataRepository` | Storing and retrieving user data. |
| `IQuestRepository` | Storing and retrieving quests and their progress. |
| `IActivityRepository` | Storing activity history and preventing duplicate entries. |
| `IAchievementRepository` | Storing earned achievements. |
| `IUserSyncGateway` | Synchronizes an authorized user’s local data with Firebase. |
| `IPushNotificationGateway` | Sends a generated message to the push notification service. |

## Domain Events

| Event | Carries | Notes |
|---|---|---|
| `ActivityRecordedDE` | user uuid, activity record uuid, steps, distance, date | Published after a new valid activity record is accepted. |
| `QuestAcceptedDE` | user uuid, quest uuid | Records the user’s acceptance of a quest. |
| `QuestProgressUpdatedDE` | user UUID, quest UUID, progress | Published when there is a significant change in progress. |
| `QuestCompletedDE` | user UUID, quest UUID | Published after the quest transitions to `COMPLETED`. |
| `AchievementUnlockedDE` | user uuid, achievement uuid | Published once after an achievement is unlocked. |
| `UserLevelChangedDE` | user uuid, old level, new level | Published when a user advances to a new level. |

Domain Events do not contain sensitive user data and must not contain raw sensor data or tokens from external services.

## Application Commands & Queries

**Commands (`AC`):**

| Area | Commands |
|---|---|
| Activity | `RecordActivityAC`, `SyncActivityAC` |
| Quests | `CreateCustomQuestAC`, `CreateRouteQuestAC`, `AcceptQuestAC`, `CancelQuestAC` |
| Achievements | `EvaluateAchievementsAC` |
| Synchronization | `SyncUserDataAC` |
| Notifications | `ScheduleActivityReminderAC` |

**Queries (`AQ`):**

| Query | Purpose |
|---|---|
| `GetCurrentActivityAQ` | Returns the current step count and cumulative activity. |
| `GetActiveQuestAQ` | Returns the active quest and its progress. |
| `ListAvailableQuestsAQ` | Returns available standard and customizable quests. |
| `GetQuestHistoryAQ` | Returns the history of completed and canceled quests. |
| `GetAchievementsAQ` | Returns earned and available achievements. |
| `GetUserStatisticsAQ` | Returns statistics for the day, week, and month. |
| `GetUserProfileAQ` | Returns the user's profile and overall progress. |

## Key Flow: Activity → Quest → Reward

1. The device or Google Fit sends activity data.
2. `Application` receives the data and passes it to the domain.
3. `ActivityRecord` is validated by `ApplyActivityDPolicy`.
4. Valid activity is saved.
5. The active quest receives a new progress update.
6. If the goal is achieved, `CompleteQuestDPolicy` allows the quest to be completed.
7. The system creates a `QuestCompletedDE`.
8. `EvaluateAchievementsService` checks the achievement conditions.
9. An `AchievementUnlockedDE` is created if a condition is met.
10. The user’s level is updated if the corresponding threshold is reached.
11. Application/Infrastructure synchronizes data with Firebase for the authorized user.
12. The next available quest can be offered to the user.

**Boundary:** The Domain is responsible for rules, invariants, and state changes. The Application is responsible for orchestration. Infrastructure is responsible for sensors, Google Fit, Google Maps, Firebase, and push services.

## Infrastructure

### Persistence

Local storage must contain at least the following:
- the user and their progress;
- quests and their status;
- progress on active quests;
- activity history;
- achievements;
- local settings.

For authorized users, local data is synchronized with Firebase. Local storage must remain functional without a constant internet connection.

### External Integrations

- Google Fit — a source of physical activity data on Android.
- Google Maps API — route planning and distance calculation.
- Firebase — remote storage and synchronization.
- Push notification service — activity reminders.
- Google Analytics / Firebase Analytics — technical and product usage statistics.

External APIs are not called directly from Domain.

## Localization

The current domain does not store interface text as business logic. Primary localization:
- Ukrainian — primary;
- English — secondary;
- Admin panel — English.

The names and descriptions of quests, achievements, and system messages must be localized at the Presentation/Application level, rather than hard-coded in the Domain.
