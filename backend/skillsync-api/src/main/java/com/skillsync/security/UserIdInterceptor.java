package com.skillsync.security;

import com.skillsync.model.User;
import com.skillsync.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * UserIdInterceptor
 * Extracts userId from the authentication context and sets it as a request attribute
 * This allows controllers to use @RequestAttribute("userId") UUID userId
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserIdInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                log.debug("Principal: {}, isAuthenticated: {}", principal.getClass().getSimpleName(), authentication.isAuthenticated());

                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    String email = userDetails.getUsername();
                    log.debug("Extracted email from authentication: {}", email);

                    // Find user by email and set userId in request
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user != null) {
                        request.setAttribute("userId", user.getUserId());
                        log.debug("Set userId attribute for email: {}, userId: {}", email, user.getUserId());
                    } else {
                        log.warn("Could not find user for email: {}", email);
                    }
                }
            } else {
                log.debug("No authentication found in security context");
            }
        } catch (Exception ex) {
            log.error("Error in UserIdInterceptor", ex);
        }

        return true;
    }
}
