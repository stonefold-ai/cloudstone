// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.server.web;

import ai.stonefold.gateway.tenancy.Tenant;
import ai.stonefold.gateway.tenancy.TenantRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Resolves the tenant from the request credential before anything else runs.
 * Requests without a resolvable tenant never reach a handler; there is no
 * "default tenant" and no fail-open path. The resolved tenant is exposed to
 * handlers as a request attribute — handlers must not re-derive it from
 * headers themselves.
 *
 * The 401 is uniform: missing, malformed and unknown credentials all get the
 * same status and body, so the response reveals nothing about tenant
 * existence or credential validity.
 */
public final class TenantAuthFilter extends OncePerRequestFilter {

    public static final String TENANT_ATTRIBUTE = "stonefold.tenant";

    private static final String BEARER_PREFIX = "Bearer ";

    private final TenantRegistry registry;

    public TenantAuthFilter(TenantRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }
        Optional<Tenant> tenant = Optional.empty();
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            tenant = registry.authenticate(authorization.substring(BEARER_PREFIX.length()).trim());
        }
        if (tenant.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            // Uniform error envelope (see ApiError). The message is generic on
            // purpose: missing, malformed and unknown credentials must be
            // indistinguishable, revealing nothing about tenant existence.
            response.getWriter().write(
                    "{\"error\":{\"code\":\"unauthorized\",\"message\":\"authentication required\"}}");
            return;
        }
        request.setAttribute(TENANT_ATTRIBUTE, tenant.get());
        chain.doFilter(request, response);
    }
}
