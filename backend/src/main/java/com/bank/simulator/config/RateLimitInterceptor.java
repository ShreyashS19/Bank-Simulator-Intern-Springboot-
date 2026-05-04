package com.bank.simulator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Layer-2 IP-based rate limiting for OTP endpoints.
 *
 * Limits (per client IP):
 *   POST /auth/forgot-password   → 5  requests per 15 minutes
 *   POST /auth/reset-password    → 10 requests per 15 minutes
 *   POST /account/forgot-pin     → 5  requests per 15 minutes
 *   POST /account/reset-pin      → 10 requests per 15 minutes
 *
 * Uses a sliding-window approach: timestamps older than the window are evicted
 * on each request. No external dependency required.
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final long WINDOW_MS = 15L * 60 * 1000; // 15 minutes

    /** Config: path-suffix → max allowed requests within WINDOW_MS */
    private static final Map<String, Integer> PATH_LIMITS = Map.of(
        "/auth/login",           5, 
            "/auth/forgot-password", 5,
            "/auth/reset-password",  10,
            "/account/forgot-pin",   5,
            "/account/reset-pin",    10
    );

    /**
     * Sliding window store: key = "IP::path-suffix"
     * Value = deque of request timestamps (epoch millis, oldest first)
     */
    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        String path = getMatchingPath(request.getRequestURI());
        if (path == null) return true; // path not rate-limited

        Integer limit = PATH_LIMITS.get(path);
        String clientIp = resolveClientIp(request);
        String key = clientIp + "::" + path;
        long now = Instant.now().toEpochMilli();

        Deque<Long> timestamps = requestLog.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // Evict entries outside the sliding window
            while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > WINDOW_MS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= limit) {
                log.warn("IP rate limit exceeded — ip={} path={} count={}/{}", clientIp, path, timestamps.size(), limit);
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        objectMapper.writeValueAsString(Map.of(
                                "success", false,
                                "message", "Too many requests. Please try again later."
                        ))
                );
                return false;
            }

            timestamps.addLast(now);
        }
        return true;
    }

    /** Match the request URI to one of our rate-limited path suffixes. */
    private String getMatchingPath(String uri) {
        if (uri == null) return null;
        for (String suffix : PATH_LIMITS.keySet()) {
            if (uri.endsWith(suffix)) return suffix;
        }
        return null;
    }

    /** Honour X-Forwarded-For if behind a proxy, otherwise use remote addr. */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
