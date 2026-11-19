package com.cooperativa.gestioncooperativa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/error", "/403", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/usuarios", "/usuarios-web", "/usuarios/**")
                                .hasAuthority("GESTIONAR_USUARIOS")
                        .requestMatchers("/roles-web", "/roles/**")
                                .hasAuthority("GESTIONAR_ROLES")
                        .requestMatchers("/cooperativas-web", "/cooperativas/**")
                                .hasAuthority("GESTIONAR_COOPERATIVAS")
                        .requestMatchers("/parroquias-web", "/parroquias/**")
                                .hasAuthority("GESTIONAR_PARROQUIAS")
                        .requestMatchers("/rutas-web").hasAuthority("CONSULTAR_TRANSPORTE")
                        .requestMatchers("/rutas/**").hasAuthority("GESTIONAR_RUTAS")
                        .requestMatchers("/autobuses-web").hasAuthority("CONSULTAR_TRANSPORTE")
                        .requestMatchers("/autobuses/**").hasAuthority("GESTIONAR_AUTOBUSES")
                        .requestMatchers("/conductores-web").hasAuthority("CONSULTAR_TRANSPORTE")
                        .requestMatchers("/conductores/**").hasAuthority("GESTIONAR_CONDUCTORES")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureUrl("/login?error")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?expired")
                        .maximumSessions(1))
                .exceptionHandling(errors -> errors.accessDeniedPage("/403"));
        return http.build();
    }
}
