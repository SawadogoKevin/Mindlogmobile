# Fix Unresolved reference: saveNom in UserRepository.kt

The user is encountering a build error `Unresolved reference: saveNom` in `UserRepository.kt`. This is because `SessionManager` is missing the `saveNom`, `savePrenom`, and `saveEmail` methods which are called in `UserRepository#updateProfile`.

## Proposed Changes

### [Component Name] Data Layer - Session Management

#### [MODIFY] [SessionManager.kt](file:///C:/DOCUMENTS/Stage/Backend/Mindlogmobile/app/src/main/java/com/mindforce/mindlog/data/local/SessionManager.kt)

Add missing individual setter methods for user profile information:
- `saveNom(nom: String?)`
- `savePrenom(prenom: String?)`
- `saveEmail(email: String?)`

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure the unresolved reference error is resolved.

### Manual Verification
- None required as this is a build-time fix.
