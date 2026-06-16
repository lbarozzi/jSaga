package it.fourlab.jsaga.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Forwards unmatched GET requests to /index.html so Angular Router handles client-side routing.
 * API calls (/api/**) and static files (*.js, *.css, etc.) are passed through unchanged.
 */
@Component
@Order(Integer.MAX_VALUE - 5)
public class SpaRoutingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        String uri = request.getRequestURI().substring(request.getContextPath().length());

        if ("GET".equalsIgnoreCase(request.getMethod())
                && !uri.startsWith("/api/")
                && !hasFileExtension(uri)) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean hasFileExtension(String uri) {
        String segment = uri.substring(uri.lastIndexOf('/') + 1);
        return segment.contains(".");
    }
}
