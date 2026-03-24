# GatherForGood
### Pray Together. Serve Together.

GatherForGood is an Android mobile application designed for Muslim communities — particularly those in non-Muslim majority countries, small towns, or university campuses — to coordinate congregational prayers and organize Islamic volunteer events.

---

## The Problem

Muslims living away from established mosques often struggle to find others for daily congregational prayers. At the same time, volunteer activities like food drives, Ramadan distributions, and blood donation camps are typically coordinated through informal WhatsApp groups with no proper structure, tracking, or approval system.

---

## The Solution

GatherForGood provides a unified platform with two core modules:

- **Prayer Gatherings** — Create, browse, filter, and join congregational prayer events by prayer type, date, time, location, and gender setting.
- **Volunteer Events** — Create and manage community service events with a structured join request and host approval workflow.

Both modules include event lifecycle management, GPS and Google Maps integration, and temporary event-based chat for coordination.

---

## Team Members

| Name | Role |
|---|---|
| Husnain | Project Lead, Architect, Android Developer, Tester |

## Setup Instructions
Follow these steps to set up the development environment:

Clone/Download Project: Extract the project source code into your local directory.

Open in Android Studio: * Launch Android Studio.

Select File > Open and navigate to the project folder.

Firebase Configuration:

Ensure the google-services.json file is present in the app/ directory.

Sync Project with Gradle Files to download dependencies.

SDK Requirements: Ensure you have Android SDK 34 (API 34) or higher installed via the SDK Manager.

---

## How To Run

Hardware/Emulator: Connect a physical Android device via USB (with USB Debugging enabled) or start a Virtual Device (AVD) via the Device Manager.

Build: Click the Build menu and select Make Project.

Run: Click the green Run (Play) button in the top toolbar.

Initial Workflow (Iteration 1):

Register: Create a new account.

Verify: Check the dummy/real email for the verification link (required for access).

Login: Sign in to reach the Home Dashboard.

Browse: Scroll through the list of prayer gatherings and click a card to view details and open Google Maps.

---

## Features

- User registration, login, and profile setup
- Create and browse prayer gatherings (Fajr, Zuhr, Asr, Maghrib, Isha, Jumu'ah)
- Filter gatherings by prayer type, date, and gender setting
- Join and leave prayer gatherings with live participant count
- Create volunteer events (Food Drive, Ramadan Distribution, Blood Donation, Charity)
- Volunteer join request and host approval workflow
- Event lifecycle management (Upcoming → Ongoing → Finished)
- Temporary event-based chat for each gathering or event
- GPS location fetching and Google Maps navigation
- Report inappropriate events or messages

---

## Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android (Java) |
| Authentication | Firebase Auth |
| Database | Firebase Firestore |
| Maps | Google Maps Intent |
| Min SDK | Android 7.0 (API 24) |

---

## Development Plan

The app is built in 3 iterations of 2 weeks each:

| Iteration | Module | Key Features |
|---|---|---|
| Sprint 1 | Authentication & Browsing | Register, login, profile setup, browse prayer gatherings |
| Sprint 2 | Prayer Gathering Management | Create, join, filter, lifecycle management |
| Sprint 3 | Volunteer & Communication | Volunteer events, approval workflow, event chat |

---

