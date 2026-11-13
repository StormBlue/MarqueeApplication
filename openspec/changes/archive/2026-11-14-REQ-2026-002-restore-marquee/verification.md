<!-- docs-architect-meta {"schema_version":1,"id":"EVID-REQ-2026-002","type":"evidence","title":"Verification for Restore and modernize segmented marquee component","status":"completed","owners":[],"sources":["openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/**"],"update_when":["Acceptance evidence or unresolved findings change"],"relations":[{"type":"validates","target":"REQ-2026-002"}],"acceptance":[{"id":"AC-1","status":"passed","validates":["AC-1","BR-marquee-001","SC-marquee-001","BR-marquee-002","SC-marquee-002","BR-marquee-003","SC-marquee-003","BR-ring-progress-001","BR-ring-progress-002"],"methods":["automated","command","runtime","inspection","screenshot"],"evidence":[{"kind":"log","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/api37-build-test.txt","description":"API37 build, lint, publication and device tests passed."},{"kind":"log","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/api30-test.txt","description":"API30 component and sample device tests passed."},{"kind":"document","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/ui-smoke.txt","description":"Sample screenshot inspection and zero idle rendering observed."},{"kind":"document","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/draw-summary.txt","description":"Measured sample draw-recording intervals from captured frame stats."}],"reason":null,"authority":null}],"evidence":[{"kind":"log","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/api37-build-test.txt","description":"API37 build, lint, publication and device tests passed."},{"kind":"log","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/api30-test.txt","description":"API30 component and sample device tests passed."},{"kind":"document","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/ui-smoke.txt","description":"Sample screenshot inspection and zero idle rendering observed."},{"kind":"document","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/draw-summary.txt","description":"Measured sample draw-recording intervals from captured frame stats."},{"kind":"document","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/review.txt","description":"Independent review findings fixed and rechecked."}],"documentation_checks":[],"reviews":[{"charter":"integrated","status":"passed","evidence":[{"kind":"document","ref":"openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/review.txt","description":"Independent review findings fixed and rechecked."}]}],"unresolved_findings":[],"verified_at":"2026-11-13T18:27:16.308227+00:00","verified_against":"sha256:1826b5ebba6c04ec879fee37f1b18cf84dd4ab5a837616f3ac4b832e7d2c7714@openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/revision.json","created_at":"2026-11-14T02:03:22+08:00","updated_at":"2026-11-14T02:27:47+08:00","completed_at":"2026-11-14T02:27:47+08:00"} -->

# EVID-REQ-2026-002: Marquee verification

## Acceptance Evidence
AC-1 passed: 32 dedicated-AVD tests, aggregate build and lint zero issues, Maven artifacts, inspected screenshot and actual idle/active frame capture. Full results are in evidence/ and docs/verification.md.

## Commands And Observations
API37: ANDROID_SERIAL=emulator-5570 ./gradlew :app:assembleDebug :app:assembleRelease :marqueeview:assembleRelease lint :marqueeview:publishToMavenLocal :marqueeview:connectedDebugAndroidTest :app:connectedDebugAndroidTest --no-parallel --console=plain
API30: ANDROID_SERIAL=emulator-5572 ./gradlew :marqueeview:connectedDebugAndroidTest :app:connectedDebugAndroidTest --no-parallel --console=plain
Both BUILD SUCCESSFUL. 15 component and 1 sample tests per device, no failures. Release AAR excludes test host. Sample has three segmented light strips; idle observation has zero frames. Sample draw-recording P95 0.144ms; no real-device or comparative FPS claim.

## Review Summary
Independent review identified detach/reveal settling and restored percentage truncation. Both fixed with passing regressions and independent follow-up. No unresolved findings.

## Documentation Disposition
- Result: updated
- Current docs: README.md and docs/verification.md; current specs merged by close.
- Standalone sync: no docs-architect repository configuration; scoped semantic and local-link inspection rather than generated documentation infrastructure.
- Current specs merged: `openspec/specs/marquee/spec.md`, `openspec/specs/ring-progress/spec.md`
- Current system or product docs reviewed: `README.md`, `docs/verification.md`
- docs-architect checks, when integrated: not applicable (docs-architect not configured)

## Completion Record
- Verified worktree captured in evidence/revision.json.
- Close: preparing dry run and atomic spec merge/archive.
- Git commit and push explicitly authorized by request.md; performed after close.
- Verified revision: `sha256:1826b5ebba6c04ec879fee37f1b18cf84dd4ab5a837616f3ac4b832e7d2c7714@openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee/evidence/revision.json`
- Verification completed at: `2026-11-13T18:27:16.308227+00:00`
- Close planning: passed
- Close result: completed
- Close completed at: `2026-11-14T02:27:47+08:00`
- Archive location: `openspec/changes/archive/2026-11-14-REQ-2026-002-restore-marquee`
- Post-close archive validation: passed
