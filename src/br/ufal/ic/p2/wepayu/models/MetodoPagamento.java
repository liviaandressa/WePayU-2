package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;


public abstract class MetodoPagamento implements Serializable {

    
    public abstract MetodoPagamento clone();
}