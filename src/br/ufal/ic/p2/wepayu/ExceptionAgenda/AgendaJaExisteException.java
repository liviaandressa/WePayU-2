package br.ufal.ic.p2.wepayu.ExceptionAgenda;

public class AgendaJaExisteException extends Exception {
    public AgendaJaExisteException() {
        super("Agenda de pagamentos ja existe");
    }
}