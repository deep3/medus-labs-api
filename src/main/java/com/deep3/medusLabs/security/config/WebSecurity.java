package com.deep3.medusLabs.security.config;

import com.deep3.medusLabs.repository.UserRepository;
import com.deep3.medusLabs.security.service.TokenAuthenticationServiceImpl;
import com.deep3.medusLabs.security.service.UserAccountService;
import com.deep3.medusLabs.security.filter.AuthenticationFilter;
import com.deep3.medusLabs.security.filter.JWTAuthenticationEntryPoint;
import com.deep3.medusLabs.security.filter.LoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private JWTAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private TokenAuthenticationServiceImpl tokenAuthenticationService;

    @Autowired
    private UserRepository accountRepository;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addExposedHeader(HttpHeaders.AUTHORIZATION);
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * Configuration method for http operations - which allowed methods, headers enabled, session management, token management
     * @param http - http object to be configured
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/h2/**").permitAll()
                .antMatchers(HttpMethod.GET, "/h2/**").permitAll()
                .antMatchers(HttpMethod.DELETE, "/h2/**").permitAll()
                .and()
                .headers().frameOptions().disable();

        http
                .cors()
                .and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)// If not authenticated, returns 401
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/").permitAll()// Allow all/any access to "/" path
                .antMatchers("/status/**").permitAll()
                .antMatchers(HttpMethod.POST, "/login").permitAll()// Allow all POST requests access to "/login"
                .anyRequest().authenticated()// Ensure all other requests are authenticated
                //.and().requiresChannel().anyRequest().requiresSecure() // Ensures all connections are https://
                .and()
                .addFilterBefore(new LoginFilter("/login", authenticationManager(),tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new AuthenticationFilter(tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class)// Filter to validate and check tokens being used as authentication
                .headers().cacheControl();
    }

    /**
     * Configuration method with AuthenticationManagerBuilder object as a parameter
     * @param auth AMB object
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceBean());
    }

    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return new UserAccountService(accountRepository);
    }

    /**
     * This bean is required by the Spring Framework. Could use other Encoders, such as Scrypt/PBKDF2 etc
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
