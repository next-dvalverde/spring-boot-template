package es.nextdigital.atm.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // El TTL de sesion se decide contra un Clock inyectado para que el store
    // sea testeable sin dormir el test.
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
