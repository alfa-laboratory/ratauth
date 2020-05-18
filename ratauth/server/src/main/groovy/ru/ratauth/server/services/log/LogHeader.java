package ru.ratauth.server.services.log;

import lombok.RequiredArgsConstructor;

/**
 * Forwarding MDC values to request headers
 */
@RequiredArgsConstructor
public enum LogHeader {
    CLIENT_ID("Client-Id", "client_id"),
    CLIENT_IP("Client-Ip", "client_ip"),
    DEVICE_ID("Device-Id", "device_id"),
    SESSION_ID("Session-Id", "session_id"),
    TRACE_ID("Trace-Id", "trace_id"),
    USER_ID("User-Id", "user_id"),
    X_B3_SPAN_ID("X-B3-SpanId", "X-B3-SpanId"),
    X_B3_TRACE_ID("X-B3-TraceId", "X-B3-TraceId");

    private final String headerValue;
    private final String mdcValue;

    public String headerVal() {
        return headerValue;
    }

    public String mdcVal() {
        return mdcValue;
    }
}
