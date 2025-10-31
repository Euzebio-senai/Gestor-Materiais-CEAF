package com.gestor.estoque;

import jakarta.persistence.*;

@Entity
@Table(name = "RETIRANTE")
public class Retirante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Deixa a DB gerar o ID
    private Long ID_RETIRANTE;
    private String NOME_RETIRANTE;
    private String SETOR;

    // Construtor para criar novos
    public Retirante() {}
    public Retirante(String nome, String setor) {
        this.NOME_RETIRANTE = nome;
        this.SETOR = setor;
    }

    // Getters
    public Long getID_RETIRANTE() { return ID_RETIRANTE; }
    public String getNOME_RETIRANTE() { return NOME_RETIRANTE; }
}