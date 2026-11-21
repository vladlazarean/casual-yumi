# Releasing

Releases are git tags. [JitPack](https://jitpack.io/#vladlazarean/casual-yumi) builds a tag the first time someone asks for it (using `jitpack.yml`) and serves it as `com.github.vladlazarean.casual-yumi:<module>:<tag>`.

## Checklist

1. **Clean `main`:** CI is green on the commit you will tag.
2. **Version:** set `yumi` in `gradle/libs.versions.toml` (for example `0.0.9`). The tag must be exactly that version, with no `v` prefix: `0.0.9`. CI refuses a tag that doesn't match.
3. **Screenshots:** `./gradlew :yumi-components:recordRoborazziDebug` if visuals changed on purpose; review and commit `yumi-components/src/test/screenshots/`.
4. **Full check:**
   ```bash
   ./gradlew check :yumi-components:verifyRoborazziDebug :catalog:assembleDebug publishToMavenLocal
   ```
5. **Catalog pass on a device:** Putty and Graphite, every accent, RTL, 200 % font scale, TalkBack on, animations off.
6. **Changelog:** move items under the new version heading. Update the version in the README install snippet.
7. **Tag and push:**
   ```bash
   git tag 0.0.9
   git push origin 0.0.9
   ```
   The `release` CI job checks the tag and asks JitPack to build it.
8. **Check JitPack:** open https://jitpack.io/#vladlazarean/casual-yumi, find the tag, and confirm the build log is green. Optionally create a GitHub Release from the tag with the changelog section as notes.

If a JitPack build fails, fix it on `main` and release a new patch version. JitPack caches tag builds, so don't move or reuse a tag.

## Trying a build in another project

```bash
./gradlew publishToMavenLocal
```

Then add `mavenLocal()` to the other project's repositories and depend on `com.github.vladlazarean.casual-yumi:yumi-bom:<version>` as usual.

## Maven Central (later)

Not set up. It needs:

- a verified namespace on the Central Portal (`io.github.vladlazarean` works without owning a domain);
- GPG signing (`signing` plugin) and a Javadoc jar (Dokka) in `PublishingConventionPlugin`;
- POM developers details.
