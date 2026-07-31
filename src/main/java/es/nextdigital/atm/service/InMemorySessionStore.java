package es.nextdigital.atm.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Token opaco UUID con TTL de 120 s y limpieza perezosa (A-6, C-1). Opaco y no
 * JWT: un cajero necesita poder revocar la sesion y el token no debe decir
 * nada de si mismo. Consecuencia asumida: dos replicas no comparten sesiones.
 */
@Component
public class InMemorySessionStore implements SessionStore {

    static final Duration TTL = Duration.ofSeconds(120);

    private final Clock clock;
    private final Map<String, Entry> sessions = new ConcurrentHashMap<>();

    private record Entry(SessionContext context, Instant expiresAt) {
    }

    public InMemorySessionStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String issue(SessionContext context) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Entry(context, clock.instant().plus(TTL)));
        return token;
    }

    @Override
    public Optional<SessionContext> resolve(String token) {
        Entry entry = sessions.get(token);
        if (entry == null) {
            return Optional.empty();
        }
        if (clock.instant().isAfter(entry.expiresAt())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(entry.context());
    }
}
