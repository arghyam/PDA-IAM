package com.socion.backend.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Configuration
public class TraceIdFilterChain extends OncePerRequestFilter {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TraceIdFilterChain.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            response.addHeader("X-TRACE-ID", UUID.randomUUID().toString());
            MDC.put("logLevelPattern", response.getHeader("X-TRACE-ID"));
            LOGGER.info("API call: " + request.getMethod() + ": " + request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
