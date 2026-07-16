# Implementation Plan - Fix Unresolved Reference in UserRepository

The user is encountering a build error `Unresolved reference: saveNom` in `UserRepository.kt`. This is because `SessionManager` lacks the methods `saveNom`, `savePrenom`, and `saveEmail` which are called in `UserRepository.updateProfile`.

## Proposed Changes

### [Component: Data Local]

#### [MODIFY] [SessionManager.kt](file:///C:/DOCUMENTS/Stage/Backend/Mindlogmobile/app/src/main/java/com/mindforce/mindlog/data/local/SessionManager.kt)
- Add `saveNom(nom: String?)` method.
- Add `savePrenom(prenom: String?)` method.
- Add `saveEmail(email: String?)` method.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure the unresolved reference errors are resolved.

### Manual Verification
- N/A (Build fix)
