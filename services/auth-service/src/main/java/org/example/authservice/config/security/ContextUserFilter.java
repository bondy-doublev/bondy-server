package org.example.authservice.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ContextUserFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String userId = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role");
            String email = request.getHeader("X-Email");

            if (userId != null) {
                ContextUser.set(Long.parseLong(userId), role, email);
            }

            filterChain.doFilter(request, response);
        } finally {
            ContextUser.clear();
        }
    }
}

