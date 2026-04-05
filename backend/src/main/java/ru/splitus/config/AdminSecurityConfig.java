package ru.splitus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import ru.splitus.security.AdminUserDetailsService;
import ru.splitus.security.InternalApiServiceTokenFilter;

/**
 * Configures Spring Security for admin authentication while preserving existing webhook and internal API flows.
 */
@Configuration
public class AdminSecurityConfig {

    /**
     * Creates the password encoder for admin credentials.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates the HTTP firewall configuration used by Spring Security.
     */
    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    /**
     * Applies the custom HTTP firewall to Spring Security.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall httpFirewall) {
        return web -> web.httpFirewall(httpFirewall);
    }

    /**
     * Creates the main security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            InternalApiServiceTokenFilter internalApiServiceTokenFilter,
            AdminUserDetailsService adminUserDetailsService) throws Exception {
        http
                .userDetailsService(adminUserDetailsService)
                .authorizeRequests()
                    .antMatchers("/admin/login").permitAll()
                    .antMatchers("/admin/**").authenticated()
                    .antMatchers("/api/internal/**", "/api/telegram/webhook/**", "/actuator/**").permitAll()
                    .anyRequest().permitAll()
                .and()
                .formLogin()
                    .loginPage("/admin/login")
                    .loginProcessingUrl("/admin/login")
                    .defaultSuccessUrl("/admin", true)
                    .failureUrl("/admin/login?error")
                    .permitAll()
                .and()
                .logout()
                    .logoutUrl("/admin/logout")
                    .logoutSuccessUrl("/admin/login?logout")
                .and()
                .csrf()
                    .ignoringAntMatchers("/api/internal/**", "/api/telegram/webhook/**", "/actuator/**");
        http.addFilterBefore(internalApiServiceTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
