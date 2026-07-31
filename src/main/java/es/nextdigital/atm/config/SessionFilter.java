package es.nextdigital.atm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.nextdigital.atm.domain.ErrorCode;
import es.nextdigital.atm.service.SessionContext;
import es.nextdigital.atm.service.SessionStore;
import es.nextdigital.atm.web.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro propio ligero en vez de Spring Security completo (decision de
 * PLAN.md &sect;1). Protege todo /api/v1/** salvo los dos endpoints que se usan
 * sin token: abrir sesion y activar la tarjeta.
 */
@Component
public class SessionFilter extends OncePerRequestFilter {

    public static final String SESSION_ATTRIBUTE = "sessionContext";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SessionStore sessionStore;
    private final ObjectMapper objectMapper;

    public SessionFilter(SessionStore sessionStore, ObjectMapper objectMapper) {
        this.sessionStore = sessionStore;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            return true;
        }
        boolean isPost = "POST".equals(request.getMethod());
        return isPost && ("/api/v1/sessions".equals(path) || "/api/v1/cards/activation".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            Optional<SessionContext> context = sessionStore.resolve(header.substring(BEARER_PREFIX.length()));
            if (context.isPresent()) {
                request.setAttribute(SESSION_ATTRIBUTE, context.get());
                filterChain.doFilter(request, response);
                return;
            }
        }
        // El advice no cubre los filtros: la respuesta de error se escribe aqui
        // con la misma forma { code, message, timestamp }.
        response.setStatus(ErrorCode.INVALID_SESSION.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                ErrorResponse.of(ErrorCode.INVALID_SESSION, "Missing, invalid or expired session"));
    }
}
