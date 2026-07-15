package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_termos_responsabilidade")
@Getter
@Setter
@NoArgsConstructor
public class TermoResponsabilidade extends Anexo {

}