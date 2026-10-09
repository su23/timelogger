# timelogger

A time tracker for Android and iOS, sharing one Kotlin Multiplatform codebase with
Compose Multiplatform for the UI. Everything is stored locally in SQLite: no account,
no sync, no network access.

## What it does

- **Tracker** — start, pause and stop a timer per activity. Running timers are persisted,
  so they survive an app restart, and several can run at once if you allow it in settings.
- **Activities** — name, icon, color and an optional group; archived rather than deleted.
- **History** — entries by day with a calendar view, editable after the fact, each with a comment.
- **Stats** — totals per activity over a chosen date range, as a bar chart.
- **Goals** — a daily, weekly or monthly minimum or maximum per activity, with progress against it.
- **Pomodoro** — work, break and long-break phases, with lengths configurable in settings.
- **Custom fields** — extra text, number or dropdown fields per activity, filled in per entry.
- **Export** — CSV, or a self-contained HTML report.

## Stack

| | |
|---|---|
| Language | Kotlin 2.2 |
| UI | Compose Multiplatform 1.10 |
| Database | SQLDelight 2.2, on SQLite both sides |
| DI | Koin 4.1 |
| Navigation | AndroidX Navigation Compose |
| Dates | kotlinx-datetime |

## Layout

```
composeApp/              the shared module: everything but the platform entry points
  commonMain/
    domain/              models and use cases
    data/                repositories, exporters, notification interface
    presentation/        screens and view models, one package per feature
    sqldelight/          .sq schema and queries
  androidMain/           SQLite driver, notifications, Koin module
  iosMain/               the same, per platform
androidApp/              Android application module
iosApp/                  iOS entry point (SwiftUI)
```

Platform-specific code is confined to `expect`/`actual` for the database driver and
notifications. The screens are shared.

## Building

Android:

```
./gradlew :androidApp:assembleDebug
```

iOS needs an Xcode project, which is not in this repository. `iosApp/` holds the SwiftUI
entry point that hosts `MainViewController` from `composeApp`, but you have to create the
Xcode target yourself and link the `ComposeApp` framework produced by:

```
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

## Status

A personal project, built for my own use.

The home-screen widgets are scaffolding only — `TimerWidgetReceiver` on Android and
`WidgetDataProvider` on iOS are placeholders, each with a comment listing what finishing
it would take (Glance on Android, a WidgetKit extension and App Groups on iOS).
