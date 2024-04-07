package com.smart.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
//import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	
	 @Bean
	    public UserDetailsService getUserDetailsService() {
	        return new UserDetailsServiceImpl();
	    }
	 
	 	
	    @Bean
	    public BCryptPasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	    
//	    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//	    	auth.authenticationProvider(getDaoAuthProvider());
//	    }
	    
	   @SuppressWarnings("removal")
	@Bean
	   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		   
		   
		   
		   http.authorizeHttpRequests().requestMatchers("/admin/**").hasRole("ADMIN")
		    .requestMatchers("/user/**").hasRole("USER")
		    .requestMatchers("/**").permitAll().and().formLogin().loginPage("/signin")
		    .loginProcessingUrl("/dosignin")
		    .defaultSuccessUrl("/user/index")
		    
		    .and().csrf().disable();
		   
		   http.authenticationProvider(authenticationProvider());
		   return http.build();
		   
	   }
	    
	    
	    @Bean
	    public DaoAuthenticationProvider authenticationProvider() {
	    	
	    	DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
	    	
	    	daoAuthenticationProvider.setUserDetailsService(getUserDetailsService());
	    	daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
	    	
	    	return daoAuthenticationProvider;
	    	
	    }

	    //configure method
	    
	    @Bean
	    public AuthenticationManager authenticationManager(
	            AuthenticationConfiguration authConfig) throws Exception {
	        return authConfig.getAuthenticationManager();
	    }
	    
	   
  
}
