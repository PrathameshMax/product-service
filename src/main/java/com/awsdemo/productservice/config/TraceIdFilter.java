package com.awsdemo.productservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";
    private static final String AMZN_TRACE_HEADER = "X-Amzn-Trace-Id";
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9=;\\-]{8,128}$");
    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        long start = System.nanoTime();
        MDC.put(MDC_KEY, traceId);
        String amznTrace = request.getHeader(AMZN_TRACE_HEADER);
        if (amznTrace != null && SAFE_ID.matcher(amznTrace).matches()) {
            MDC.put("amznTraceId", amznTrace);
        }
        response.setHeader(HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            if (!request.getRequestURI().startsWith("/actuator")) {
                log.info("http method={} path={} status={} durationMs={}",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), tookMs);
            }
            MDC.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(HEADER);
        if (incoming != null && SAFE_ID.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }
}
