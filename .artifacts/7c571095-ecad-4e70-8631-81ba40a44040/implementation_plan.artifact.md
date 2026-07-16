# Fix Unresolved Reference in UserRepository

The build error `Unresolved reference: saveNom` in `UserRepository.kt` occurs because `SessionManager` lacks individual setter methods for `nom`, `prenom`, and `email`. These methods are used in `UserRepository#updateProfile` to sync the local session with updated user data.

## Proposed Changes

### Data Layer

#### [MODIFY] [SessionManager.kt](file:///C:/DOCUMENTS/Stage/Backend/Mindlogmobile/app/src/main/java/com/mindforce/mindlog/data/local/SessionManager.kt)
Add the following methods to `SessionManager` to allow updating individual profile fields:
- `saveNom(nom: String)`
- `savePrenom(prenom: String)`
- `saveEmail(email: String)`

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference error is resolved.

### Manual Verification
- None required as this is a build-time fix.
