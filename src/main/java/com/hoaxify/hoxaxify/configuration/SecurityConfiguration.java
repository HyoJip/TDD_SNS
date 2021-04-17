package com.hoaxify.hoxaxify.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hoaxify.hoxaxify.service.AuthUserService;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	AuthUserService authUserService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		
		http.httpBasic().authenticationEntryPoint(new BasicAuthenticateEntryPoint());
		
		http
			.authorizeRequests().antMatchers(HttpMethod.POST, "/api/1.0/login").authenticated()
								.antMatchers(HttpMethod.PUT, "/api/1.0/users/{id:[\\d]+}").authenticated()
								.antMatchers(HttpMethod.POST, "/api/1.0/hoaxes/**").authenticated()
								.antMatchers(HttpMethod.DELETE, "/api/1.0/hoaxes/**").authenticated()
			.and()
			.authorizeRequests().anyRequest().permitAll();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.headers().frameOptions().disable();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(authUserService).passwordEncoder(passwordEncoder());
	}
	
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
