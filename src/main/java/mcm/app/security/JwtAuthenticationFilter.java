package mcm.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // Cookie name must match what AuthController sets
    private static final String JWT_COOKIE_NAME = "JWT_TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUsernameFromJwt(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authenticated user: {}, authorities: {}, request URI: {}",
                        username, userDetails.getAuthorities(), request.getRequestURI());

            } else {
                logger.warn("No valid JWT found for request URI: {}", request.getRequestURI());
            }

        } catch (Exception e) {
            logger.error("JWT authentication error for request URI: {} - {}", request.getRequestURI(), e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parse JWT from Authorization header or cookie.
     */
    private String parseJwt(HttpServletRequest request) {
        // 1️⃣ Check Authorization header
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            logger.debug("JWT found in Authorization header for request URI: {}", request.getRequestURI());
            return headerAuth.substring(7);
        }

        // 2️⃣ Check cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    logger.debug("JWT found in cookie '{}' for request URI: {}", JWT_COOKIE_NAME, request.getRequestURI());
                    return cookie.getValue();
                }
            }
        }

        logger.debug("No JWT found in headers or cookies for request URI: {}", request.getRequestURI());
        return null;
    }
}