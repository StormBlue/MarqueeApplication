# Delta Spec: marquee

## MODIFIED Requirements

### Requirement `BR-marquee-003`: Small usable open-source project
The project SHALL contain one marquee library and one sample, including a gradient gallery with distinct palettes, labeled fixed directions and an arbitrary-angle slider, current API/build README without migration notes, local Maven AAR/source publication and focused device tests. The library SHALL restore data/style/playback intent using stable view IDs and expose the mean target intensity as a ProgressBar accessibility range. Sample recreation SHALL retain the selected percentage and stopped state. Main SHALL be committed and pushed as the latest baseline without rewriting unrelated history.

#### Scenario `SC-marquee-003`: Consumer and sample integration
- **WHEN** View state or sample Activity is recreated
- **THEN** target brightness/style/playback intent and a selected 53% are preserved.
- **WHEN** the release library is published locally
- **THEN** its test host is excluded and its only runtime dependency is Kotlin stdlib.

#### Scenario `SC-marquee-004`: Gradient gallery demonstration
- **WHEN** the sample gallery is opened
- **THEN** 0°, 45°, 90° and 135° use distinct palettes and each segment restarts its gradient.
- **WHEN** the custom angle slider is changed to 312° and the Activity is recreated
- **THEN** the gradient, label and slider retain 312°.
- **WHEN** the README demonstration is viewed
- **THEN** the animated GIF visibly demonstrates the palettes and arbitrary angle adjustment.
