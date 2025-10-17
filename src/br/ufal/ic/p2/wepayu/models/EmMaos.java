package br.ufal.ic.p2.wepayu.models;


public class EmMaos extends MetodoPagamento {

    
    public EmMaos() {}

    
    @Override
    public String toString() {
        return "emMaos";
    }

    
    @Override
    public MetodoPagamento clone() {
        return new EmMaos();
    }
}