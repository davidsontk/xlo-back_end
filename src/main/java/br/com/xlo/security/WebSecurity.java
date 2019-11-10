package br.com.xlo.security;

import static br.com.xlo.security.SecurityConstants.SIGN_UP_URL;
import br.com.xlo.usuario.UserDetailsServiceImpl;
import java.util.Arrays;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 *
 * @author Davidson
 */
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private UserDetailsServiceImpl userDetailsService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public WebSecurity(UserDetailsServiceImpl userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/").permitAll();

        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), userDetailsService))
                //this disables session creation on Spring Security
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

//        http.cors().and().csrf().disable().authorizeRequests()
//                .antMatchers(HttpMethod.POST, "/login").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .addFilter(new JWTAuthenticationFilter(authenticationManager(),userDetailsService))
//                .addFilter(new JWTAuthorizationFilter(authenticationManager()))
//                 //this disables session creation on Spring Security
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

}
