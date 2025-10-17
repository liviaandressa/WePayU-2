package br.ufal.ic.p2.wepayu.ExceptionPonto;

public class DataInvalidaException extends Exception {
    public DataInvalidaException() {
        super("Data invalida.");
    }
    public DataInvalidaException(String tipo) {
        super("Data " + tipo + " invalida.");
    }
}