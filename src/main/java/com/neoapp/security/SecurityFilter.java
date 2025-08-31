package com.neoapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> SKIP_FILTER_URLS = Arrays.asList(
            "/auth/v1/**",
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    );

    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityFilter(TokenService tokenService, CustomUserDetailsService customUserDetailsService) {
        this.tokenService = tokenService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Processing request: {} {}", method, requestURI);

        if (shouldSkipFilter(requestURI)) {
            logger.debug("Skipping token validation for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("Skipping token validation for OPTIONS request to: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = recoverToken(request);

        if (token != null && !token.trim().isEmpty()) {
            try {
                String login = tokenService.validateToken(token);

                if (login != null && !login.trim().isEmpty()) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(login);

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication set for user: {}", login);
                }
            } catch (UsernameNotFoundException e) {
                logger.warn("User not found for token: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("Error during token validation for URI: {}", requestURI, e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipFilter(String requestURI) {
        boolean shouldSkip = SKIP_FILTER_URLS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));

        if (shouldSkip) {
            logger.debug("Request URI '{}' matches skip pattern", requestURI);
        }
        return shouldSkip;
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            String token = authHeader.substring(7);
            logger.debug("Token found in Authorization header");
            return token;
        }

        logger.debug("No valid Bearer token found in Authorization header");
        return null;
    }
}