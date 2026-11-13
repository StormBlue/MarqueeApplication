# Delta Spec: ring-progress

## ADDED Requirements

### Requirement `BR-ring-progress-001`: Small Android ring progress library
The library SHALL expose one Kotlin View with XML and Java-compatible properties for finite progress clamped to [0,max], positive finite max/stroke width, colors, start angle, direction, caps, determinate transition duration and indeterminate state. Invalid values SHALL fail explicitly. The default size SHALL be 96dp plus padding, constrained by the parent. Geometry SHALL remain centered and visible within rectangular and padded layouts.

#### Scenario `SC-ring-progress-001`: Public API and drawing
- **WHEN** progress exceeds max or max decreases
- **THEN** target and drawn progress are clamped, and accessibility reports the updated range.
- **WHEN** dimensions or padding change
- **THEN** the centered ring adapts without allocating bitmaps.

### Requirement `BR-ring-progress-002`: Lifecycle and integration
The View SHALL reuse drawing objects, invalidate only for changes/active animation, cancel animation while detached or not visible, restore target/style by stable view ID, and expose progress accessibility semantics. One sample and README SHALL demonstrate use and explain the breaking legacy migration. Published release AAR SHALL exclude the debug test host.

#### Scenario `SC-ring-progress-002`: Animation cleanup and restoration
- **WHEN** an animated target changes
- **THEN** transition starts at the currently displayed value.
- **WHEN** hidden or detached
- **THEN** no animator remains active.
- **WHEN** restored or shown again
- **THEN** determinate target is restored, and enabled indeterminate animation may restart.
