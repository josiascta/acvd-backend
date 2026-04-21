package br.edu.ifpb.ads.acvd.config;

import br.edu.ifpb.ads.acvd.service.TokenService;
import br.edu.ifpb.ads.acvd.entity.Role;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public SecurityConfig(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        //apagar metodo de delete após testes
                        .requestMatchers(HttpMethod.DELETE, "/users/test/email/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customSuccessHandler())
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String email = oidcUser.getEmail();
            String nome = oidcUser.getFullName();
            String picture = oidcUser.getPicture();

            Role role;
            if (email.equals("josiasjt3@gmail.com") || email.equals("davir9647@gmail.com") || email.equals("eduardojose71953@gmail.com") || email.endsWith("@ifpb.edu.br")) {
                role = Role.SERVIDOR;
            } else if (email.equals("orienracjos@gmail.com") || email.endsWith("@academico.ifpb.edu.br")) {
                role = Role.DISCENTE;
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso permitido apenas para contas institucionais IFPB.");
                return;
            }

            User user = userRepository.findByEmail(email).map(existingUser -> {
                existingUser.setFotoDePerfil(picture);
                existingUser.setNome(nome);
                return userRepository.save(existingUser);
            }).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setNome(nome);
                newUser.setFotoDePerfil(picture);
                newUser.setRole(role);
                return userRepository.save(newUser);
            });

            String token = tokenService.generateToken(user);

            String targetUrl = "http://localhost:3000/login-success?token=" + token;

            response.sendRedirect(targetUrl);
        };
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
            }
        };
    }
}