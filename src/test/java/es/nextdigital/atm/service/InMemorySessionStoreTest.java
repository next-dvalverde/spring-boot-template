package es.nextdigital.atm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemorySessionStoreTest {

    // Reloj desplazable: probar el TTL sin dormir el test.
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-07-31T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final InMemorySessionStore store = new InMemorySessionStore(clock);
    private final SessionContext context =
            new SessionContext(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    @Test
    void unknownTokenDoesNotResolve() {
        assertTrue(store.resolve("no-such-token").isEmpty());
    }

    @Test
    void tokenExpiresAfterTtl() {
        String token = store.issue(context);
        clock.advance(InMemorySessionStore.TTL.plusSeconds(1));
        assertTrue(store.resolve(token).isEmpty());
    }

    @Test
    void tokenResolvesWithinTtl() {
        String token = store.issue(context);
        clock.advance(InMemorySessionStore.TTL.minusSeconds(1));
        assertEquals(context, store.resolve(token).orElseThrow());
    }

    @Test
    void tokensAreOpaqueAndUnique() {
        assertNotEquals(store.issue(context), store.issue(context));
    }
}
