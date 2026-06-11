package com.bank.simulator.service.impl;

import com.bank.simulator.dto.AadhaarVerificationResponse;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.service.AadhaarVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class AadhaarVerificationServiceImpl implements AadhaarVerificationService {

    private final RestTemplate restTemplate;

    @Value("${kyc.provider}")
    private String kycProvider;

    @Value("${kyc.sandbox.base-url}")
    private String sandboxBaseUrl;

    @Value("${kyc.sandbox.api-key}")
    private String sandboxApiKey;

    @Value("${kyc.sandbox.secret-key}")
    private String sandboxSecretKey;

    // Cached access token and its expiry
    private String cachedAccessToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public AadhaarVerificationServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AadhaarVerificationResponse generateOtp(String aadhaarNumber) {
        if (aadhaarNumber == null || !aadhaarNumber.matches("\\d{12}")) {
            throw new BusinessException("Invalid Aadhaar number. Must be exactly 12 digits.");
        }

        String maskedAadhaar = maskAadhaar(aadhaarNumber);
        log.info("Generating Aadhaar OTP for masked number: {}", maskedAadhaar);

        if ("sandbox".equalsIgnoreCase(kycProvider)) {
            return generateOtpViaSandbox(aadhaarNumber, maskedAadhaar);
        } else {
            throw new BusinessException("Unsupported KYC provider: " + kycProvider,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AadhaarVerificationResponse verifyOtp(String aadhaarNumber, String otp, String clientId) {
        if (aadhaarNumber == null || !aadhaarNumber.matches("\\d{12}")) {
            throw new BusinessException("Invalid Aadhaar number. Must be exactly 12 digits.");
        }
        if (otp == null || !otp.matches("\\d{6}")) {
            throw new BusinessException("Invalid OTP. Must be exactly 6 digits.");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new BusinessException("Client ID (reference ID) is required for OTP verification.");
        }

        String maskedAadhaar = maskAadhaar(aadhaarNumber);
        log.info("Verifying Aadhaar OTP for masked number: {}, referenceId: {}", maskedAadhaar, clientId);

        if ("sandbox".equalsIgnoreCase(kycProvider)) {
            return verifyOtpViaSandbox(aadhaarNumber, otp, clientId, maskedAadhaar);
        } else {
            throw new BusinessException("Unsupported KYC provider: " + kycProvider,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== Sandbox.co.in Authentication ====================

    /**
     * Authenticate with Sandbox.co.in to obtain an access token.
     * Tokens are cached for 23 hours (they expire in 24h).
     */
    private synchronized String getAccessToken() {
        // Return cached token if still valid (with 1-hour buffer)
        if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiry)) {
            log.debug("Using cached Sandbox access token");
            return cachedAccessToken;
        }

        log.info("Authenticating with Sandbox.co.in to obtain access token");

        String url = sandboxBaseUrl + "/authenticate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", sandboxApiKey);
        headers.set("x-api-secret", sandboxSecretKey);
        headers.set("x-api-version", "1.0");

        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String accessToken = extractStringField(responseBody, "access_token");

                if (accessToken == null) {
                    // Try nested "data" field
                    Object dataObj = responseBody.get("data");
                    if (dataObj instanceof Map) {
                        accessToken = extractStringField((Map<String, Object>) dataObj, "access_token");
                    }
                }

                if (accessToken == null || accessToken.isBlank()) {
                    log.error("No access_token in Sandbox authenticate response: {}", responseBody.keySet());
                    throw new BusinessException("Failed to authenticate with KYC provider.",
                            HttpStatus.SERVICE_UNAVAILABLE);
                }

                // Cache token for 23 hours (tokens last 24h)
                cachedAccessToken = accessToken;
                tokenExpiry = Instant.now().plusSeconds(23 * 3600);

                log.info("Successfully obtained Sandbox access token");
                return cachedAccessToken;
            } else {
                log.error("Sandbox authentication failed with status: {}", response.getStatusCode());
                throw new BusinessException("Failed to authenticate with KYC provider.",
                        HttpStatus.SERVICE_UNAVAILABLE);
            }
        } catch (HttpClientErrorException e) {
            log.error("Sandbox authentication client error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            // Invalidate cached token
            cachedAccessToken = null;
            tokenExpiry = Instant.EPOCH;
            throw new BusinessException("KYC provider authentication failed. Please contact support.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Sandbox authentication", e);
            cachedAccessToken = null;
            tokenExpiry = Instant.EPOCH;
            throw new BusinessException("Failed to connect to KYC provider.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    // ==================== Sandbox.co.in OKYC Implementation ====================

    private AadhaarVerificationResponse generateOtpViaSandbox(String aadhaarNumber, String maskedAadhaar) {
        String url = sandboxBaseUrl + "/kyc/aadhaar/okyc/otp";

        // Get authenticated access token first
        String accessToken = getAccessToken();
        HttpHeaders headers = buildSandboxHeaders(accessToken);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("@entity", "in.co.sandbox.kyc.aadhaar.okyc.otp.request");
        body.put("aadhaar_number", aadhaarNumber);
        body.put("consent", "Y");
        body.put("reason", "KYC verification for customer onboarding");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Extract reference_id from Sandbox response
                String referenceId = extractStringField(responseBody, "reference_id");
                if (referenceId == null) {
                    // Try nested "data" object
                    Object dataObj = responseBody.get("data");
                    if (dataObj instanceof Map) {
                        referenceId = extractStringField((Map<String, Object>) dataObj, "reference_id");
                    }
                }

                log.info("Aadhaar OTP sent successfully for masked: {}, referenceId: {}", maskedAadhaar, referenceId);

                return AadhaarVerificationResponse.builder()
                        .verified(false)
                        .maskedAadhaar(maskedAadhaar)
                        .message("OTP sent to Aadhaar-linked mobile number")
                        .clientId(referenceId)
                        .build();
            } else {
                log.error("Unexpected response from Sandbox OTP generation: status={}", response.getStatusCode());
                throw new BusinessException("Failed to generate OTP. Please try again later.");
            }

        } catch (HttpClientErrorException e) {
            log.error("Sandbox OTP generation client error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());

            // If 401/403, token may have expired — invalidate and retry once
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.warn("Sandbox token may be expired, clearing cache and retrying...");
                cachedAccessToken = null;
                tokenExpiry = Instant.EPOCH;
                // Don't retry in this call — let the user try again
                throw new BusinessException("Authentication expired. Please try again.");
            }

            String errorMessage = parseErrorMessage(e.getResponseBodyAsString());
            throw new BusinessException(errorMessage != null ? errorMessage : "Invalid Aadhaar number or service unavailable.");
        } catch (HttpServerErrorException e) {
            log.error("Sandbox OTP generation server error: status={}", e.getStatusCode());
            throw new BusinessException("KYC provider service temporarily unavailable. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Aadhaar OTP generation", e);
            throw new BusinessException("Failed to connect to KYC provider. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private AadhaarVerificationResponse verifyOtpViaSandbox(String aadhaarNumber, String otp,
                                                             String clientId, String maskedAadhaar) {
        String url = sandboxBaseUrl + "/kyc/aadhaar/okyc/otp/verify";

        // Get authenticated access token first
        String accessToken = getAccessToken();
        HttpHeaders headers = buildSandboxHeaders(accessToken);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("@entity", "in.co.sandbox.kyc.aadhaar.okyc.request");
        body.put("reference_id", clientId);
        body.put("otp", otp);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Check for verification status
                Integer statusCode = extractIntField(responseBody, "code");
                String status = extractStringField(responseBody, "status");

                // Sandbox returns code 200 and status "SUCCESS" on valid verification
                boolean verified = (statusCode != null && statusCode == 200)
                        || "SUCCESS".equalsIgnoreCase(status)
                        || "success".equalsIgnoreCase(status);

                if (!verified) {
                    // Check nested data for verification status
                    Object dataObj = responseBody.get("data");
                    if (dataObj instanceof Map) {
                        String dataStatus = extractStringField((Map<String, Object>) dataObj, "status");
                        verified = "SUCCESS".equalsIgnoreCase(dataStatus)
                                || "verified".equalsIgnoreCase(dataStatus);
                    }
                }

                if (verified) {
                    log.info("Aadhaar OTP verified successfully for masked: {}", maskedAadhaar);
                    return AadhaarVerificationResponse.builder()
                            .verified(true)
                            .maskedAadhaar(maskedAadhaar)
                            .message("Aadhaar verified successfully")
                            .clientId(clientId)
                            .build();
                } else {
                    log.warn("Aadhaar OTP verification failed for masked: {}", maskedAadhaar);
                    throw new BusinessException("OTP verification failed. The OTP may be incorrect or expired.");
                }
            } else {
                log.error("Unexpected response from Sandbox OTP verification: status={}", response.getStatusCode());
                throw new BusinessException("OTP verification failed. Please try again.");
            }

        } catch (HttpClientErrorException e) {
            log.error("Sandbox OTP verification client error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());

            // If 401/403, token may have expired — invalidate
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                cachedAccessToken = null;
                tokenExpiry = Instant.EPOCH;
                throw new BusinessException("Authentication expired. Please try again.");
            }

            String errorMessage = parseErrorMessage(e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new BusinessException(errorMessage != null ? errorMessage : "Invalid OTP. Please check and try again.");
            } else {
                throw new BusinessException(errorMessage != null ? errorMessage : "OTP verification failed. Try again.");
            }
        } catch (HttpServerErrorException e) {
            log.error("Sandbox OTP verification server error: status={}", e.getStatusCode());
            throw new BusinessException("KYC provider service temporarily unavailable. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Aadhaar OTP verification", e);
            throw new BusinessException("Failed to verify OTP. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Build headers for Sandbox OKYC API calls.
     * Uses the access token obtained from /authenticate endpoint.
     * Note: Sandbox docs specify the Authorization header without "Bearer" prefix.
     */
    private HttpHeaders buildSandboxHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        headers.set("x-api-key", sandboxApiKey);
        headers.set("x-api-version", "1.0");
        return headers;
    }

    /**
     * Mask Aadhaar number: 123456789012 → XXXX-XXXX-9012
     */
    private String maskAadhaar(String aadhaarNumber) {
        if (aadhaarNumber == null || aadhaarNumber.length() != 12) {
            return "XXXX-XXXX-XXXX";
        }
        return "XXXX-XXXX-" + aadhaarNumber.substring(8);
    }

    private String extractStringField(Map<String, Object> map, String field) {
        Object value = map.get(field);
        return value != null ? value.toString() : null;
    }

    private Integer extractIntField(Map<String, Object> map, String field) {
        Object value = map.get(field);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Try to extract a meaningful error message from the KYC provider's JSON error response.
     * Never logs or returns raw Aadhaar numbers — only sanitized messages.
     */
    private String parseErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            // Look for "message" field in the response
            if (responseBody.contains("\"message\"")) {
                int start = responseBody.indexOf("\"message\"");
                int colonIndex = responseBody.indexOf(":", start);
                int valueStart = responseBody.indexOf("\"", colonIndex + 1);
                int valueEnd = responseBody.indexOf("\"", valueStart + 1);
                if (valueStart >= 0 && valueEnd > valueStart) {
                    return responseBody.substring(valueStart + 1, valueEnd);
                }
            }
        } catch (Exception e) {
            log.debug("Could not parse error message from KYC provider response");
        }
        return null;
    }
}
