<!-- docs-architect-meta {"schema_version":1,"id":"SPEC-marquee","type":"product-spec","title":"Marquee product specification","status":"active","owners":[],"sources":[],"update_when":["Observable marquee behavior changes"],"relations":[],"evidence":[],"affected_docs":[],"verified_at":null,"verified_against":null} -->

# Marquee Product Specification

## Requirements

### Requirement `BR-marquee-001`: Segmented marquee light strip
The library SHALL provide one Kotlin MarqueeView with XML and Java/Kotlin APIs for 1–256 lights, finite per-light intensities in [0,1], finite clamped progress, spacing, corners, colors, per-segment diagonal gradients with configurable angle/start/end colors, reveal duration, shimmer width/duration and logical direction. Public array inputs/outputs and saved state SHALL be isolated copies. Layout SHALL respect parent constraints, padding, size changes and host RTL settings.

#### Scenario `SC-marquee-001`: Light rendering and validation
- **WHEN** progress is 0.375 on four lights
- **THEN** target intensities are [1,0.5,0,0].
- **WHEN** a 45 or 135 degree gradient is enabled
- **THEN** it repeats independently inside each light, and cached shaders are reused across animation frames.
- **WHEN** invalid arrays, counts or nonfinite values are supplied
- **THEN** the call fails explicitly without modifying existing targets.

### Requirement `BR-marquee-002`: Sequential reveal and efficient shimmer
The strip SHALL transition light intensities sequentially from currently displayed values, then optionally shimmer using a single animator slot. Drawing SHALL reuse buffers/Paint and cache gradient shaders outside animation/drawing. Flat rendering SHALL use one rectangle per segment; gradient rendering SHALL use at most three. No bitmap/mask/offscreen pipeline or explicit draw-path object allocations are allowed. Stop, hidden ancestors, window invisibility and detach SHALL cancel animation and settle displayed values to targets. Requested playback SHALL restart shimmer after showing/reattaching; all-dark strips and system-disabled animation SHALL not start loops.

#### Scenario `SC-marquee-002`: Animation lifecycle
- **WHEN** a reveal is retargeted
- **THEN** it begins at current displayed intensities.
- **WHEN** a reveal is interrupted by detach
- **THEN** reattachment shows targets without freezing a partial reveal.
- **WHEN** stopped or hidden
- **THEN** no animator continues running.

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
