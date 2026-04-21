package com.bank.simulator.exception;

import com.bank.simulator.auth.AuthErrorConstants;

public class AccountDeactivatedException extends RuntimeException {

    public AccountDeactivatedException() {
        super(AuthErrorConstants.ACCOUNT_DEACTIVATED_MESSAGE);
    }

    public String getErrorCode() {
        return AuthErrorConstants.ACCOUNT_DEACTIVATED_CODE;
    }
}
