# Release Checklist

This document outlines the step-by-step process for releasing a new version of Redis Utils.

## Release Types

- **PATCH Release (1.0.x)** - Bug fixes and minor updates
- **MINOR Release (1.x.0)** - New features, backward-compatible
- **MAJOR Release (x.0.0)** - Breaking changes

## Pre-Release Checklist

### 1. Code Quality & Testing

- [ ] All tests pass locally
  ```bash
  mvn clean verify
  ```

- [ ] Code coverage meets threshold (>80%)
  ```bash
  mvn clean test jacoco:report
  # Review: target/site/jacoco/index.html
  ```

- [ ] No compiler warnings or errors
  ```bash
  mvn clean compile -Xlint:all
  ```

- [ ] All Javadoc builds without warnings
  ```bash
  mvn javadoc:javadoc
  ```

- [ ] Integration tests pass with Testcontainers
  ```bash
  mvn verify -Pfailsafe
  ```

- [ ] Manual testing completed for new features
  - [ ] Test with example application
  - [ ] Test with real Redis instance
  - [ ] Test with Redis cluster (if applicable)

### 2. Documentation

- [ ] README.md updated with new features
- [ ] CHANGELOG.md updated with all changes
  - [ ] Categorize changes: Added, Changed, Deprecated, Removed, Fixed, Security
  - [ ] Add migration notes for breaking changes
  - [ ] Link to GitHub issues/PRs

- [ ] API documentation (Javadoc) reviewed and updated
- [ ] Example application updated with new features
- [ ] Migration guide created (for MAJOR releases)
- [ ] Configuration properties documented

### 3. Version Management

- [ ] Decide on version number following SemVer
  - MAJOR: Breaking API changes
  - MINOR: New features, backward-compatible
  - PATCH: Bug fixes only

- [ ] Update version in all `pom.xml` files
  ```bash
  mvn versions:set -DnewVersion=1.x.x
  mvn versions:commit
  ```

- [ ] Update version references in README.md
- [ ] Update version in CHANGELOG.md
- [ ] Create Git tag with version
  ```bash
  git tag -a v1.x.x -m "Release version 1.x.x"
  ```

### 4. Dependencies

- [ ] Review and update dependencies
  ```bash
  mvn versions:display-dependency-updates
  mvn versions:display-plugin-updates
  ```

- [ ] Check for security vulnerabilities
  ```bash
  mvn dependency:tree
  mvn org.owasp:dependency-check-maven:check
  ```

- [ ] Test with updated dependencies
- [ ] Document any dependency version changes in CHANGELOG

### 5. Security Review

- [ ] Run security scan
  ```bash
  mvn org.owasp:dependency-check-maven:check
  ```

- [ ] Review code for security issues
  - [ ] No hardcoded credentials
  - [ ] No sensitive data in logs
  - [ ] Safe serialization/deserialization
  - [ ] Proper input validation

- [ ] Check for known CVEs in dependencies

### 6. Performance Testing

- [ ] Run performance benchmarks
- [ ] Compare with previous release
- [ ] Document any performance changes
- [ ] Profile memory usage
- [ ] Test with large datasets

### 7. Compatibility Testing

- [ ] Test with supported Java versions (17, 21)
  ```bash
  # Use different JDK versions
  JAVA_HOME=/path/to/jdk-17 mvn clean verify
  JAVA_HOME=/path/to/jdk-21 mvn clean verify
  ```

- [ ] Test with supported Spring Boot versions (3.3+)
- [ ] Test with supported Redis versions (5.0, 6.0, 7.0)
- [ ] Test on different operating systems (Windows, Linux, macOS)

## Release Process

### 1. Prepare Release Branch

```bash
# Create release branch from develop/main
git checkout -b release/v1.x.x

# Update version numbers
mvn versions:set -DnewVersion=1.x.x
mvn versions:commit

# Update CHANGELOG.md
# Update README.md version references
# Commit changes
git add .
git commit -m "chore: prepare release v1.x.x"

# Push release branch
git push origin release/v1.x.x
```

### 2. Final Testing

- [ ] CI/CD pipeline passes on release branch
- [ ] All automated tests pass
- [ ] Manual smoke testing completed
- [ ] Security scan clean
- [ ] Documentation reviewed

### 3. Create Release Tag

```bash
# Tag the release
git tag -a v1.x.x -m "Release version 1.x.x

Features:
- Feature 1
- Feature 2

Bug Fixes:
- Fix 1
- Fix 2

See CHANGELOG.md for full details."

# Push tag
git push origin v1.x.x
```

### 4. Build Release Artifacts

```bash
# Clean build with all tests
mvn clean verify

# Build without tests (if needed for release artifacts)
mvn clean package -DskipTests

# Generate sources and javadoc JARs
mvn source:jar javadoc:jar

# Sign artifacts (if publishing to Maven Central)
mvn gpg:sign-and-deploy-file \
  -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
  -DrepositoryId=ossrh \
  -DpomFile=pom.xml \
  -Dfile=target/redis-utils-*.jar
```

### 5. Deploy to Maven Central (Optional)

- [ ] Configure GPG signing
  ```bash
  gpg --gen-key
  gpg --keyserver hkp://keyserver.ubuntu.com --send-keys YOUR_KEY_ID
  ```

- [ ] Configure Maven settings.xml
  ```xml
  <servers>
    <server>
      <id>ossrh</id>
      <username>YOUR_USERNAME</username>
      <password>YOUR_PASSWORD</password>
    </server>
  </servers>
  ```

- [ ] Deploy to OSSRH
  ```bash
  mvn clean deploy -P release
  ```

- [ ] Login to https://oss.sonatype.org/
  - [ ] Close staging repository
  - [ ] Release to Maven Central
  - [ ] Verify artifacts appear in search.maven.org (may take 2-10 hours)

### 6. Create GitHub Release

- [ ] Go to https://github.com/mehrdadfalahati/redis-utils/releases/new
- [ ] Select tag: `v1.x.x`
- [ ] Release title: `v1.x.x - [Release Name]`
- [ ] Copy relevant CHANGELOG.md section to release notes
- [ ] Add release highlights
- [ ] Attach release artifacts (JARs)
  - [ ] redis-utils-core-1.x.x.jar
  - [ ] redis-utils-spring-boot-starter-1.x.x.jar
  - [ ] redis-utils-examples-1.x.x.jar
  - [ ] Source code (auto-generated by GitHub)

- [ ] Mark as pre-release if RC/beta
- [ ] Publish release

### 7. Update Documentation

- [ ] Update GitHub wiki (if exists)
- [ ] Update project website (if exists)
- [ ] Update README badges (version, build status)
- [ ] Update any external documentation

### 8. Merge Release Branch

```bash
# Merge to main/master
git checkout main
git merge release/v1.x.x
git push origin main

# Merge back to develop
git checkout develop
git merge release/v1.x.x
git push origin develop

# Delete release branch
git branch -d release/v1.x.x
git push origin --delete release/v1.x.x
```

### 9. Post-Release Tasks

- [ ] Verify Maven Central deployment (after 2-10 hours)
  ```
  https://search.maven.org/artifact/com.github.mehrdadfalahati/redis-utils-spring-boot-starter
  ```

- [ ] Update example application to use new version
- [ ] Announce release on:
  - [ ] GitHub Discussions
  - [ ] Twitter/X
  - [ ] Dev.to / Medium (if applicable)
  - [ ] Company blog (if applicable)

- [ ] Monitor for issues:
  - [ ] GitHub Issues
  - [ ] Stack Overflow
  - [ ] Community channels

- [ ] Update version to next SNAPSHOT
  ```bash
  mvn versions:set -DnewVersion=1.x.x-SNAPSHOT
  mvn versions:commit
  git commit -am "chore: prepare for next development iteration"
  git push
  ```

## Hotfix Release Process (PATCH)

For urgent bug fixes that can't wait for the next regular release:

1. **Create Hotfix Branch**
   ```bash
   git checkout -b hotfix/v1.0.x main
   ```

2. **Apply Fix**
   - Make minimal changes
   - Write tests
   - Update CHANGELOG.md

3. **Version Bump**
   ```bash
   mvn versions:set -DnewVersion=1.0.x
   ```

4. **Test Thoroughly**
   ```bash
   mvn clean verify
   ```

5. **Release**
   - Follow standard release process
   - Expedite review if critical

6. **Merge Back**
   ```bash
   git checkout main
   git merge hotfix/v1.0.x
   git checkout develop
   git merge hotfix/v1.0.x
   ```

## Rollback Procedure

If critical issues are discovered after release:

1. **Assess Severity**
   - Is it a security issue?
   - Does it break existing functionality?
   - Can it wait for a hotfix?

2. **Communication**
   - [ ] Post issue on GitHub
   - [ ] Update release notes with known issues
   - [ ] Notify users via GitHub Discussions
   - [ ] Send security advisory if applicable

3. **Quick Fix (if possible)**
   - [ ] Create hotfix branch
   - [ ] Apply fix
   - [ ] Release PATCH version

4. **Deprecate Release (if severe)**
   - [ ] Mark release as deprecated on GitHub
   - [ ] Update README with warning
   - [ ] Remove from recommended versions

## Release Schedule

- **PATCH releases**: As needed for critical bugs
- **MINOR releases**: Quarterly (Q1, Q2, Q3, Q4)
- **MAJOR releases**: Annually, with 6-month deprecation notice

## Communication Templates

### Release Announcement Template

```markdown
# Redis Utils v1.x.x Released! 🎉

We're excited to announce the release of Redis Utils v1.x.x!

## What's New

### ✨ New Features
- Feature 1
- Feature 2

### 🐛 Bug Fixes
- Fix 1
- Fix 2

### 📚 Documentation
- Documentation improvements

## Getting Started

Maven:
\`\`\`xml
<dependency>
    <groupId>com.github.mehrdadfalahati</groupId>
    <artifactId>redis-utils-spring-boot-starter</artifactId>
    <version>1.x.x</version>
</dependency>
\`\`\`

## Full Changelog

See [CHANGELOG.md](CHANGELOG.md) for complete details.

## Upgrading

[Migration guide if needed]

## Thank You

Thanks to all contributors who made this release possible!
```

### Security Advisory Template

```markdown
# Security Advisory: [CVE-ID if assigned]

## Summary
Brief description of the vulnerability

## Affected Versions
- redis-utils-core: versions X.X.X to Y.Y.Y
- redis-utils-spring-boot-starter: versions X.X.X to Y.Y.Y

## Fixed Versions
- redis-utils-core: Z.Z.Z+
- redis-utils-spring-boot-starter: Z.Z.Z+

## Severity
[Critical / High / Medium / Low]

## Description
Detailed description of the vulnerability

## Mitigation
Steps to mitigate if immediate upgrade is not possible

## Upgrade Instructions
How to upgrade to the fixed version

## Credits
Credit to reporter if applicable
```

## Checklist Summary

Before releasing, ensure:

- ✅ All tests pass
- ✅ Documentation updated
- ✅ CHANGELOG.md complete
- ✅ Version numbers updated
- ✅ Security scan clean
- ✅ Dependencies reviewed
- ✅ Compatibility tested
- ✅ Performance validated
- ✅ GitHub release created
- ✅ Artifacts published
- ✅ Announcement sent
- ✅ Post-release monitoring active

## Additional Resources

- [Semantic Versioning](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)
- [Maven Central Guide](https://central.sonatype.org/publish/)
- [GitHub Releases Guide](https://docs.github.com/en/repositories/releasing-projects-on-github)
