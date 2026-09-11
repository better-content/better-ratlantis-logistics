# Ratlantis Logistics

Better Content's Rats compatibility rules and logistics crafting components.

## Verification

Run `./gradlew verifyFull` before committing. It runs deterministic policy checks, seven
GameTests, and runtime mixin-refmap validation. The GameTests exercise the registered feeding
handler, guaranteed taming and owner assignment, non-bait rejection, the actual transformed Rats
crop-harvesting goal, and nearest eligible seed consumption.

The test-only eligible seed tag lives in `src/gameTestFixtures/resources` and is excluded from the
runtime JAR. Crop fixtures disable harvested drops only during their synchronous action so the
assertions measure consumption of the supplied input. These tests cover this mod's integration
rules; they do not claim comprehensive upstream Rats or Pretty Pipes transport coverage.

Each GameTest invocation retains a fresh world, logs, and `execution.json` under
`build/gametest/<run-token>/`. Verification requires the current token and every reviewed test ID
in `gametest/profiles/full.txt` to register, execute, and pass. `verifyGameTestEvidenceGuard` checks
missing, stale, incomplete, duplicate, and failed results without starting Forge. Its script is
repository-local under `gametest/`; the existing `gradle/` wrapper symlink is not used for test logic.
