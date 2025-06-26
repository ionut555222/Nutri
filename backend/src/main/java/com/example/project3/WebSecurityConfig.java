package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    
    @Autowired
    private StaffDetailsService staffDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(staffDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Increased strength
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health/**").permitAll()
                
                // Public browsing endpoints
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/fruits/**").permitAll()
                
                // Customer-only endpoints
                .requestMatchers("/api/orders/**").hasAuthority("ROLE_CUSTOMER")
                .requestMatchers("/api/chat/**").hasAuthority("ROLE_CUSTOMER")
                .requestMatchers("/api/customer/**").hasAuthority("ROLE_CUSTOMER")
                
                // Staff endpoints
                .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                .requestMatchers("/api/staff/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN", "ROLE_EMPLOYEE")
                
                // Owner-only endpoints
                .requestMatchers("/api/owner/**").hasAuthority("ROLE_OWNER")
                
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Public static resources
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/login", "/signup", "/customer-info", "/access-denied").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                
                // Admin interface
                .requestMatchers("/admin/login", "/admin/logout").permitAll()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "EMPLOYEE", "OWNER")
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .successHandler((request, response, authentication) -> {
                    // Simplified success handler
                    response.sendRedirect("/admin");
                })
                .failureUrl("/admin/login?error")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}

 