package br.ufal.ic.p2.wepayu.ExceptionEmpregados;

public class TaxaSindicalNaoPodeSerNulaException extends Exception {
    public TaxaSindicalNaoPodeSerNulaException() {
        super("Taxa sindical nao pode ser nula.");
    }
}