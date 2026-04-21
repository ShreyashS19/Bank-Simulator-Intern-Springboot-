package com.bank.simulator.auth;

public final class AuthErrorConstants {

    public static final String ACCOUNT_DEACTIVATED_CODE = "ACCOUNT_DEACTIVATED";
    public static final String ACCOUNT_DEACTIVATED_PARAM = "account_deactivated";
    public static final String OAUTH_FAILED_PARAM = "oauth_failed";
    public static final String ACCOUNT_DEACTIVATED_MESSAGE =
            "Your account has been deactivated due to suspicious activity related to your account. Please contact bank.simulator.issue@gmail.com to activate your account.";

    private AuthErrorConstants() {
    }
}
