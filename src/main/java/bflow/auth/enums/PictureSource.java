package bflow.auth.enums;

/**
 * Origin of the user's profile picture. Determines whether the Cognito
 * sync flow is allowed to overwrite it.
 */
public enum PictureSource {
    /** Picture sourced from Google's OAuth profile claim. */
    GOOGLE,
    /** Picture manually uploaded by the user to S3. */
    S3,
    /** No picture set. */
    NONE
}
