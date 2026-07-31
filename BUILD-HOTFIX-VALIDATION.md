# Build Hotfix Validation

Version: 1.0.1

- Replaced nonexistent Slimefun dependency version `2026.1`.
- Uses published addon API baseline `com.github.SlimefunGuguProject:Slimefun4:2025.1.2` from JitPack.
- Slimefun Legacy remains the intended runtime core and preserves the established addon API.
- XML parsing of `pom.xml` succeeded.
- GitHub Actions workflow YAML parsing succeeded.
- No Java sources, Slimefun item IDs, persistent-data keys, or resource recipes changed in this hotfix.

A complete Maven build could not be executed in this sandbox because Maven and outbound dependency access are unavailable here. The correction directly addresses the dependency-resolution failure shown in GitHub Actions.
