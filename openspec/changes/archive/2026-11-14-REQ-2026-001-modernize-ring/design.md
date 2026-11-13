# Design

Use Kotlin and one native View, plus one sample app. A two-primitive ring does not justify Compose as a mandatory runtime dependency or a second rendering implementation. Preserve the hardware accelerated Canvas path and reuse Paint / RectF; remove bitmap composition, shaders, logging and legacy business-specific APIs. ValueAnimator only runs while attached and visible. Bounds are computed cheaply during drawing to cover padding and resize without stateful caches.

This is a breaking 2.0 API: the repository contained marquee/shimmer and bitmap gauge experiments, not a coherent ring API. Replace those modules/assets with ringprogress and document migration explicitly. Runtime dependency is Kotlin stdlib only. Publish locally with official maven-publish; do not pretend Maven Central coordinates are live.

Documentation sync mode: README owns setup/API/migration; docs/verification.md owns observed results; OpenSpec owns behavior and change history. No repository-wide docs-architect configuration or generated indexes are added to this small component repository. The docs-architect tool reports missing_config, so semantic documentation/link inspection is used with standalone dev-spec-flow checks.
