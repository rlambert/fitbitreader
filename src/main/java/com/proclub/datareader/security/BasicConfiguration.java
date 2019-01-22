package com.proclub.datareader.security;

import com.proclub.datareader.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class BasicConfiguration extends WebSecurityConfigurerAdapter {

    AppConfig _config;

    @Autowired
    public BasicConfiguration(AppConfig config) {
        _config = config;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        String adminUser = _config.getAdminUser();
        String adminPassword = _config.getAdminPassword();

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser(adminUser)
                .password(encoder.encode(adminPassword))
                .roles("ADMIN");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String apiPrefix = _config.getApiRestBase();

        http.authorizeRequests()

                // these, and only these URLs are unprotected
                .antMatchers("/version",
                        "/apidocs/**",
                        "/docs/**",
                        "/version/**",
                        "/auth/**",
                        apiPrefix + "/**"
                ).permitAll()

                .anyRequest().authenticated()
                .and().formLogin()
                //.loginPage("/login")
                .permitAll();

    }
}
