package org.example.authservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        logger.info("Processing request: {} {} with Authorization header: {}", 
                   request.getMethod(), request.getRequestURI(), 
                   requestTokenHeader != null ? "present" : "absent");

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
                logger.info("Extracted username from token: {}", username);
            } catch (Exception e) {
                logger.error("Unable to get JWT Token", e);
            }
        } else {
            logger.debug("No Bearer token found in Authorization header");
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("Validating token for user: {}", username);
            
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // if token is valid configure Spring Security to manually set authentication
                if (jwtUtil.validateToken(jwtToken, username)) {
                    logger.info("Token is valid for user: {}", username);
                    
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the
                    // Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    logger.warn("Token validation failed for user: {}", username);
                }
            } catch (Exception e) {
                logger.error("Error loading user details for: {}", username, e);
            }
        } else {
            logger.debug("Either username is null or authentication already exists");
        }
        
        logger.info("Continuing with filter chain");
        chain.doFilter(request, response);
    }
}