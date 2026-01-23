package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.entity.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(User user) {
        var now = Instant.now();
        var expiresIn = 30000L;
        boolean isProfileComplete = user.getMatricula() != null && !user.getMatricula().isBlank();

        var claims = JwtClaimsSet.builder()
                .issuer("acvd-backend")
                .subject(user.getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", user.getRole().name())
                .claim("email", user.getEmail())
                .claim("complete", isProfileComplete)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
