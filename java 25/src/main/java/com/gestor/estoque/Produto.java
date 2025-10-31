package com.gestor.estoque;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PRODUTO")
public class Produto {

    @Id
    private Long ID_PRODUTO;
    private String ITEM;
    private Integer QUANTIDADE_POSSUIDA;

    // Getters e Setters
    public Long getID_PRODUTO() { return ID_PRODUTO; }
    public String getITEM() { return ITEM; }
    public Integer getQUANTIDADE_POSSUIDA() { return QUANTIDADE_POSSUIDA; }
    
    // Setter para atualizar a quantidade
    public void setQUANTIDADE_POSSUIDA(Integer QUANTIDADE_POSSUIDA) {
        this.QUANTIDADE_POSSUIDA = QUANTIDADE_POSSUIDA;
    }
}