package org.example.authservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
//    private final ContextUserFilter contextUserFilter;
//
//    public SecurityConfig(ContextUserFilter contextUserFilter) {
//        this.contextUserFilter = contextUserFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .addFilterAfter(contextUserFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
