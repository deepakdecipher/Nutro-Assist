package it.neutro.assist.admin;

/**
 * Request body for phone OTP login.
 *
 * @param idToken Firebase ID token obtained after the client confirms the SMS OTP
 *                via {@code ConfirmationResult.confirm(otp)}
 */
public record PhoneLoginRequest(String idToken) {}
