# Better Ratlantis Logistics

Better Content-owned Forge mod that makes Ratlantis the origin of scalable logistics.

Requires Java 17, Minecraft 1.20.1, and Forge 47.4.13. Build from this repository with its own checked-in Gradle wrapper:

```sh
./gradlew --no-daemon verifyFull stageRuntimeJar
```

`verifyFull` includes deterministic tests and the focused Forge GameTest lane. Its test-only fixture resources do not ship in the production JAR. The reobfuscated runtime artifact is `build/libs/better-ratlantis-logistics-0.1.0.jar`.

Local verification does not authorize deployment or pack tests. Generated build, cache, and runtime data stay untracked.

Licensed under GPL-3.0-or-later; see [LICENSE](LICENSE). Contribution and validation requirements are in [AGENTS.md](AGENTS.md).
