package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;


public class AgendaPagamento {
    private String descricao;

    
    public AgendaPagamento(String descricao) {
        this.descricao = descricao;
    }

    
    public boolean devePagar(LocalDate data) {

        
        return false;
    }

    
    public String getDescricao() {
        return descricao;
    }
}