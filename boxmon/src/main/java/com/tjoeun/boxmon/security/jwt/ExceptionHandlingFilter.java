package com.tjoeun.boxmon.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver resolver;

    public ExceptionHandlingFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            // 이미 커밋된 응답이면 더 못 건드림
            if (response.isCommitted()) throw ex;

            // @RestControllerAdvice / ErrorController 흐름으로 위임
            resolver.resolveException(request, response, null, ex);
        }
    }
}

