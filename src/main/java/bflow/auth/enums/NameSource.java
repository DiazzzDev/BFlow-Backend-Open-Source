package bflow.auth.enums;

/**
 * Origin of the user's display name. Determines whether the Cognito
 * sync flow is allowed to overwrite it.
 */
public enum NameSource {
    /** Name sourced from Google's OAuth profile claim. */
    GOOGLE,
    /** Name manually set by the user via {@code PATCH /me}. */
    USER
}
