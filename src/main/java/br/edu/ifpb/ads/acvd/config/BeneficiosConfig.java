package br.edu.ifpb.ads.acvd.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ifpb.beneficios")
@Data
public class BeneficiosConfig {

    private Double valorDiariaCnpq;
    private Double tetoInscricao;

    private Map<String, Double> percentuais;
}