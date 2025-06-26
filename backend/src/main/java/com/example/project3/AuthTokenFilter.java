package com.example.project3;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        logger.debug("Processing request: {} {}", request.getMethod(), requestPath);
        
        // Skip JWT processing for admin pages - they use form-based authentication
        if (requestPath.startsWith("/admin")) {
            logger.debug("Skipping JWT filter for admin path: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                logger.debug("JWT token found in request to: {}", requestPath);
                
                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("Valid JWT token for user: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication set for user: {} with authorities: {}", 
                        username, userDetails.getAuthorities());
                } else {
                    logger.warn("Invalid JWT token provided for request: {} {}", request.getMethod(), requestPath);
                    // Clear any existing authentication context
                    SecurityContextHolder.clearContext();
                }
            } else {
                logger.debug("No JWT token found for request: {} {}", request.getMethod(), requestPath);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication for request {} {}: {}", 
                request.getMethod(), requestPath, e.getMessage());
            // Clear authentication context on error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
} 