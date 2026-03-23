package br.edu.ifpb.ads.acvd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.util.Date;

public record RegisterDTO(String email,
                          String matricula,
                          String telefone,
                          @NotBlank(message = "O CPF é obrigatório")
                          @CPF(message = "O formato ou o dígito verificador do CPF é inválido")
                          String numeroCpf,
                          @NotBlank(message = "O RG é obrigatório")
                          @Size(min = 9, max = 9, message = "O RG deve ter entre 5 e 20 caracteres")
                          String numeroRg,
                          Date dataNascimento,
                          String turmaPeriodo,
                          String curso) {}
                          
