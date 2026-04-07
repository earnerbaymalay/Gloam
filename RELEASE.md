# Release Process

## Versioning

Gloam uses [Semantic Versioning](https://semver.org/): `MAJOR.MINOR.PATCH`

- **MAJOR:** Breaking changes (database migrations, permission changes)
- **MINOR:** New features (new screens, new prompt categories)
- **PATCH:** Bug fixes (UI fixes, solar calculation corrections)

## Release Checklist

### Before Release
- [ ] All tests passing (`./gradlew test`)
- [ ] Lint clean (`./gradlew lintDebug`)
- [ ] No hardcoded debug logs in release build
- [ ] ProGuard rules validated
- [ ] Database migrations tested (if schema changed)
- [ ] Documentation updated
- [ ] `versionCode` and `versionName` bumped in `app/build.gradle.kts`

### Release Build
```bash
./gradlew assembleRelease
./gradlew bundleRelease
```

### Post-Release
- [ ] Create GitHub Release with tag
- [ ] Write release notes
- [ ] Update ROADMAP.md

---

*Every release is a commitment to our users' privacy.*
