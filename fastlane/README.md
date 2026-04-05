fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

### clean

```sh
[bundle exec] fastlane clean
```

Clean the project build artifacts

### test

```sh
[bundle exec] fastlane test
```

Run unit tests and verify branch coverage (≥80%)

### bump_version

```sh
[bundle exec] fastlane bump_version
```

Increment versionCode and optionally set versionName. Options: version_name (String)

### build

```sh
[bundle exec] fastlane build
```

Build the production release AAB

### deploy

```sh
[bundle exec] fastlane deploy
```

Upload the production AAB and metadata to Google Play. Options: track (default: production), rollout (default: 1.0)

### promote

```sh
[bundle exec] fastlane promote
```

Promote a release between tracks. Options: from_track (default: internal), to_track (default: alpha), rollout (default: 1.0)

### release

```sh
[bundle exec] fastlane release
```

Deploy to Google Play and push to remote. Options: deploy_track (default: production), rollout (default: 1.0), version_name (optional)

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
