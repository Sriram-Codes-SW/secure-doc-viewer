package com.example.securedocviewer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.List;

/**
 * Authentication is a server-side HTTP session carried in an httpOnly
 * cookie, so the session id is never visible to JavaScript, never sent in a
 * custom header, and never returned by any endpoint. Because a cookie is
 * sent automatically, every state-changing request also needs a CSRF token
 * (double-submit cookie read by Angular's HttpClient).
 *
 * <p>Authorization is enforced here, not in the UI: the frontend only hides
 * what a role can't use.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CsrfTokenRepository csrfTokenRepository,
                                                   SecurityContextRepository securityContextRepository,
                                                   SessionRegistry sessionRegistry,
                                                   SecurityErrorResponses errors) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.changeSessionId())
                        // Unlimited concurrent sessions, but registered, so an
                        // admin can list and revoke them.
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry)
                        .expiredSessionStrategy(errors))
                .headers(headers -> headers
                        // The API only ever returns JSON and PNG tiles, so nothing it serves
                        // needs to run script, load resources or be framed.
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
                        // Tile URLs carry signed tokens; never send them onward in a Referer.
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER))
                        .permissionsPolicyHeader(permissions -> permissions.policy(
                                "camera=(), microphone=(), geolocation=(), payment=()")))
                .authorizeHttpRequests(auth -> auth
                        // Liveness/readiness for monitoring: status only, no details.
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/documents").hasAnyRole("PUBLISHER", "ADMIN")
                        // Per-document owner/admin checks for edits live in DocumentService.
                        .requestMatchers("/api/users/**").hasAnyRole("PUBLISHER", "ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .addFilterAfter(new PasswordChangeRequiredFilter(),
                        org.springframework.security.web.access.intercept.AuthorizationFilter.class)
                .requestCache(cache -> cache.disable())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /** Delegating encoder stores "{bcrypt}..." so the algorithm can be upgraded later without a migration. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.sameSite("Strict").path("/"));
        return repository;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /** Lets the registry forget sessions when they are invalidated or time out. */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * Applied by {@code AuthController} on a successful sign-in: new session
     * id (defeats fixation) and register it for admin visibility/revocation.
     * The CSRF token is rotated by AuthController itself — see there for why
     * Spring's CsrfAuthenticationStrategy isn't used.
     */
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(SessionRegistry sessionRegistry) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(),
                new RegisterSessionAuthenticationStrategy(sessionRegistry)));
    }
}
