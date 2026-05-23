# Druk Product Requirements Document

## Implementation Docs

- [Tech Stack](docs/TECH_STACK.md)
- [Azure Setup](docs/AZURE_SETUP.md)
- [Branching and Releases](docs/BRANCHING_AND_RELEASES.md)

## 1. Overview

Druk is an Android app that helps people pace alcoholic drinks around an intentional target state. The signature experience is built around "Druk": an estimated 0.05% BAC zone that many people describe as a pleasant social buzz.

The app is not designed to help users drink more recklessly, claim medical precision, or determine whether someone can drive. It is designed to help users understand their likely alcohol curve, avoid accidentally overshooting, and enjoy a session with more control.

## 2. Product Positioning

### Product Promise

Enjoy the night you meant to have.

### Core Value

Druk turns vague drinking intuition into a simple pacing system:

- Pick the state you want.
- Log what you drink.
- See where your estimated BAC is headed.
- Get nudges for when to wait, sip, hydrate, eat, or stop.

### Tone

The product should feel calm, adult, useful, and lightly playful. It should avoid both moralizing and glamorizing unsafe drinking.

## 3. Goals

### User Goals

- Choose a preferred drinking state before or during a session.
- Understand the estimated effect of each drink over time.
- Stay near a chosen BAC range instead of accidentally overshooting.
- Get clear timing guidance for when another drink would likely move them toward or away from their target.
- Review past sessions and learn what pacing works best for them.

### Business/Product Goals

- Create a differentiated alcohol pacing app centered on intentional enjoyment.
- Build trust through transparent estimates, clear limitations, and responsible safety boundaries.
- Enable future personalization based on session outcomes and user feedback.

## 4. Non-Goals

Druk will not:

- Tell users they are safe to drive.
- Provide legal, medical, or clinical BAC measurement.
- Encourage users to exceed unsafe BAC levels.
- Optimize for maximum alcohol consumption.
- Recommend drinking when the user is already above safety thresholds.
- Support underage drinking.

## 5. Target Users

### Primary User: Social Drinker

Someone going to a party, bar, dinner, or event who wants to feel good without losing control of the night.

Needs:

- Fast drink logging.
- Simple guidance.
- Minimal friction in social settings.
- Confidence that they are not accidentally overdoing it.

### Secondary User: Planner

Someone who wants to pre-plan a drinking session around duration, drink type, and desired intensity.

Needs:

- Session planning.
- Drink forecasts.
- Target arrival time.
- Wind-down guidance.

### Secondary User: Reflective Optimizer

Someone who wants to learn from past nights and calibrate their future pacing.

Needs:

- Session history.
- Peak BAC estimate.
- Time in target range.
- Next-day feedback.
- Personalized recommendations.

## 6. Core Concepts

### BAC Estimate

Druk estimates blood alcohol concentration using user profile inputs, drink logs, elapsed time, and alcohol metabolism assumptions. The estimate should always be presented as approximate.

### Target State

A target state is a BAC range associated with a subjective feeling. Presets should be friendly, clear, and bounded by safety logic.

Example presets:

| Preset | Estimated BAC Range | Description |
| --- | --- | --- |
| Light Buzz | 0.02%-0.03% | Relaxed but subtle |
| Social Glow | 0.03%-0.04% | Warm, talkative, easy |
| Druk | 0.04%-0.06% | The signature balanced zone |
| Party Mode | 0.06%-0.07% | More intense, still bounded |
| Custom | User-defined | Requires stronger warnings and limits |

### Session

A session is a time-bounded drinking event. Each session includes profile context, target state, drinks, estimates, alerts, and recap data.

### Pacing Recommendation

A pacing recommendation tells the user what action best supports their target:

- Wait before drinking.
- Sip slowly.
- Drink now if below target.
- Hydrate.
- Eat.
- Stop alcohol for the session.

## 7. MVP Scope

### Must Have

- Account creation and login.
- User profile setup.
- Start a drinking session.
- Select target state, including Druk.
- Log drinks quickly.
- Estimate current BAC.
- Forecast BAC over time.
- Show target range on a live chart.
- Recommend next drink timing.
- Send pacing alerts.
- End session and show recap.
- View basic session history.
- Safety disclaimers and hard stop states.

### Should Have

- Saved favorite drinks.
- "Same again" quick action.
- Hydration and food reminders.
- Custom target range with warnings.
- User feedback after a session.
- Next-day hangover or mood check-in.

### Could Have

- Group mode.
- Wear OS companion.
- Calendar/event integration.
- Rideshare shortcut.
- Venue-based drink presets.
- Barcode or menu scanning.

### Out of Scope for MVP

- Real BAC hardware integration.
- AI cocktail recognition from images.
- Social feeds.
- Public leaderboards.
- Medical-grade analytics.

## 8. User Experience Requirements

### First-Time Onboarding

The user should understand three things within the first minute:

- Druk estimates, not measures, BAC.
- The app helps pace a session around a target.
- The app must never be used to decide if driving is safe.

Required onboarding inputs:

- Date of birth or age verification.
- Weight.
- Sex/body profile input required by BAC model.
- Drinking experience/tolerance, optional.
- Safety preference, such as personal max BAC cap.

### Login

Authentication should support:

- Email and password.
- Google Sign-In.
- Optional biometric unlock after account creation.

### Home Screen

When no session is active, the home screen should show:

- Start Session primary action.
- Last session summary.
- Favorite target state.
- Session history entry point.
- Profile/settings entry point.

When a session is active, the home screen should become the live session dashboard.

### Start Session Flow

Required steps:

1. Choose target state.
2. Confirm session start time.
3. Choose expected duration.
4. Add context: ate recently, hydration, energy level.
5. Start live tracking.

The flow should be skippable where possible. A user in a bar should be able to start tracking in under 20 seconds.

### Target State Selector

The selector should use preset cards or segmented controls with clear BAC ranges. Druk should be visually emphasized as the signature option.

Custom targets should:

- Require deliberate selection.
- Show safety warnings above moderate levels.
- Enforce an app-level max target limit.

### Drink Logging

Drink logging must be fast and forgiving.

Required drink fields:

- Drink type.
- Volume.
- ABV.
- Time consumed or started.
- Optional notes.

Quick actions:

- Beer.
- Wine.
- Shot.
- Cocktail.
- Custom.
- Same again.
- Half drink.

The user should be able to edit or delete drinks because mistakes are likely during a night out.

### Live Session Dashboard

The dashboard should show:

- Estimated current BAC.
- BAC trend: rising, stable, or falling.
- Target zone.
- Projected curve for the next several hours.
- Next recommended drink time.
- Logged drinks timeline.
- Current recommendation.
- Hydration/food state.
- End Session action.

The primary recommendation should be plain-language and actionable:

- "Wait 18 min."
- "You're in the Druk zone."
- "Sip slowly to hold steady."
- "Take a water break."
- "Alcohol recommendations paused for tonight."

### Alerts

Alerts should be configurable and useful without becoming noisy.

Alert types:

- Entering target zone.
- Leaving target zone.
- Approaching upper limit.
- Recommended wait time complete.
- Hydration reminder.
- Food reminder.
- Session cap reached.
- Wind-down reminder.

The app should avoid alerts that pressure the user to drink. Alerts can say a drink would fit the pacing plan, but should not use urgent language to encourage alcohol consumption.

### Session Recap

After ending a session, show:

- Total drinks.
- Estimated peak BAC.
- Time in target range.
- Time above target.
- Session duration.
- Drink timeline.
- User rating: too light, good, too much.
- Optional next-day check-in.

## 9. Safety and Ethical Requirements

Safety is a product requirement, not a legal footer.

### Hard Rules

- Never display "safe to drive."
- Never estimate legal impairment status.
- Never recommend more alcohol above the app safety cap.
- Never support users below legal drinking age.
- Always label BAC as estimated.
- Always allow the user to stop alcohol recommendations.

### Warning States

The app should become more assertive when:

- Estimated BAC is above target.
- Drinks are logged too quickly.
- The user selects a high custom target.
- The model confidence is low.
- The user has been drinking for many hours.

### Safety Actions

The app should provide:

- Stop for tonight button.
- Emergency contact shortcut.
- Rideshare shortcut.
- Water and food reminders.
- Friend check-in option.

## 10. BAC Model Requirements

The MVP should use a transparent Widmark-style estimate with conservative assumptions.

Inputs:

- Weight.
- Sex/body profile coefficient.
- Alcohol consumed in grams.
- Time since drink.
- Metabolism rate.
- Session context modifiers, if supported later.

Outputs:

- Estimated current BAC.
- Projected BAC curve.
- Time to target range.
- Time to below selected thresholds.

The model must expose uncertainty in user-facing language. Example:

"Estimated BAC: 0.047%. Real BAC can vary based on food, metabolism, medication, sleep, and pour accuracy."

## 11. Data Requirements

### User

- User ID.
- Authentication provider.
- Age verification status.
- Weight.
- Sex/body profile input.
- Safety cap.
- Notification preferences.
- Favorite drinks.
- Created date.

### Session

- Session ID.
- User ID.
- Start time.
- End time.
- Target state.
- Target BAC range.
- Context inputs.
- Estimated peak BAC.
- Time in target range.
- User session rating.

### Drink

- Drink ID.
- Session ID.
- Type.
- Name.
- Volume.
- ABV.
- Alcohol grams.
- Logged time.
- Consumed time.

### Alert

- Alert ID.
- Session ID.
- Type.
- Trigger condition.
- Sent time.
- User action, if any.

## 12. Success Metrics

### Activation

- Percentage of users who complete profile setup.
- Percentage of users who start first session.
- Time to first logged drink.

### Engagement

- Sessions per active user.
- Drinks logged per session.
- Percentage of sessions with target state selected.
- Notification opt-in rate.

### Product Quality

- Percentage of sessions with at least one pacing recommendation followed.
- Percentage of time users remain within chosen target range.
- Session recap completion rate.
- User-rated session satisfaction.

### Safety

- Percentage of sessions where alcohol recommendations are paused due to safety cap.
- Percentage of users using rideshare/emergency/friend shortcuts.
- Reports of confusing or unsafe guidance.

## 13. Key Screens

### 1. Welcome / Age Gate

Purpose: establish trust, verify age eligibility, and explain that BAC is estimated.

Primary action: Create Account or Sign In.

### 2. Profile Setup

Purpose: collect required inputs for BAC estimation.

Primary action: Save Profile.

### 3. Home

Purpose: start or resume a session quickly.

Primary action: Start Session.

### 4. Target Selector

Purpose: choose the desired state.

Primary action: Start with selected target.

### 5. Drink Logger

Purpose: log a drink in seconds.

Primary action: Add Drink.

### 6. Live Session

Purpose: track current state, forecast trajectory, and show next action.

Primary action: follow current pacing recommendation.

### 7. Session Recap

Purpose: help user learn from the session.

Primary action: Rate Session.

### 8. History

Purpose: review patterns across sessions.

Primary action: open session detail.

## 14. MVP Release Criteria

The MVP is ready when:

- A user can create an account and complete profile setup.
- A user can start a session with a target BAC range.
- A user can log common drink types quickly.
- The app shows estimated BAC and projected BAC curve.
- The app gives next-drink timing guidance.
- The app sends at least basic pacing notifications.
- The app can end a session and show a recap.
- Session history persists across app restarts.
- Safety cap behavior is implemented and tested.
- The app never presents BAC estimates as legal or medical facts.

## 15. Open Questions

- What exact BAC range should define the branded Druk zone?
- Should Druk support anonymous local-only use, or require login for all users?
- What is the initial maximum custom target BAC allowed by the app?
- How conservative should the metabolism assumptions be?
- Should the first version support multiple countries with different drinking-age rules?
- Should profile inputs use biological sex labels, body composition labels, or a more guided explanation?
- How much personalization should happen automatically versus through explicit user feedback?

## 16. Future Opportunities

- Personalized BAC calibration based on session ratings.
- Smarter drink recommendations based on known favorites.
- Group session mode with privacy controls.
- Wear OS quick logging and alerts.
- Optional breathalyzer hardware integrations.
- Venue or event modes.
- AI-assisted drink entry from menu text.
- Exportable personal insights.
