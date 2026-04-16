package com.bank.simulator.entity;

/**
 * Identifies which feature an OtpEntity record belongs to.
 * A single shared otp table serves both use-cases.
 */
public enum OtpPurpose {
    PASSWORD_RESET,
    PIN_RESET
}
