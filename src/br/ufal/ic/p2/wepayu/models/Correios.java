package br.ufal.ic.p2.wepayu.models;


public class Correios extends MetodoPagamento {

    
    public Correios() {}

    
    @Override
    public MetodoPagamento clone() {
        return new Correios();
    }

    
    @Override
    public String toString() {
        return "correios";
    }
}