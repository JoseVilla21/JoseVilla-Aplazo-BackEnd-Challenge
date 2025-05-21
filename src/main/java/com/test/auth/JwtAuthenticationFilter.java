package com.test.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService uds) {
        this.jwtService = jwtService;
        this.userDetailsService = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        try {
            log.debug("Processing JWT authentication filter for request: {}", request.getRequestURI());

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.info("No Authorization header or does not start with Bearer");
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);
            log.debug("JWT extracted. Username from token: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("No authentication found in context, loading user details for username: {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(jwt, userDetails)) {
                    log.info("JWT token valid for user: {}", username);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                    sendUnauthorizedResponse(response, request.getRequestURI(), "Token inválido");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error en autenticación JWT para la request: {}", request.getRequestURI(), e);
            sendUnauthorizedResponse(response, request.getRequestURI(), "Token inválido o no autorizado");
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String path, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("APZ000007")
                .error("UNAUTHORIZED")
                .timestamp(Instant.now().getEpochSecond())
                .message(message)
                .path(path)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
