# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Every user has a balance, which starts at 0.00 € and holds the amount of money that the group owes the user.
  `POST /api/users/{userId}/expenses` records an expense and adds its amount to the balance, and
  `GET /api/users/{userId}/balance` reads the balance. The domain rejects an amount of 0.00 € or less and an amount
  with more than two decimal places.

### Changed

- The README describes how the group buys coffee beans from the kitty and how a user who buys coffee with their own
  money declares the purchase as an expense. It states that students first see the app in the lectures on agentic
  software engineering and explains how to set up IntelliJ IDEA.
- The README no longer tells readers to clone the repository, because they have already cloned it when they open the
  README in IntelliJ IDEA. The setup instructions start with `mise trust` and continue with `mise install`, and every
  command in the README has its own code block.
- `gradle check` runs ktlint on the root `build.gradle.kts` and `settings.gradle.kts` and on the included build
  `build-logic`, in addition to the subprojects. ktlint found four formatting violations in
  `java-conventions.gradle.kts`, which are fixed.

### Fixed

- A successful build no longer ends with the deprecation notice that detekt 2.0.0-alpha.6 causes. Until a detekt
  release includes the fix from detekt/detekt#9577, `org.gradle.warning.mode=none` in `gradle.properties` hides all
  Gradle deprecation warnings.
- IntelliJ could not start the Gradle daemon when it ran Gradle on a JDK older than 23, because such a JDK rejects the
  option `--sun-misc-unsafe-memory-access=allow` in `org.gradle.jvmargs`. `gradle/gradle-daemon-jvm.properties` now
  requires JDK 25 for the daemon. When Gradle finds no JDK 25 on the machine, it downloads a JDK 25 from the URLs in
  this file. The foojay toolchain resolver in `settings.gradle.kts` looked up these URLs.

## [0.1.0] - 2026-10-31

### Added

- A REST API that lists and creates users, adds a coffee to a user's coffee count, and stores the users and their
  coffee counts in PostgreSQL. A multi-module Gradle build runs ktlint, detekt, and the tests.

[0.1.0]: https://github.com/se-uhd/coffee-kitty/releases/tag/v0.1.0
