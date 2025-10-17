package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class EmpregadoAssalariado extends Empregado {
    private BigDecimal salarioMensal;

    
    public EmpregadoAssalariado() {
        super();
    }

    
    public EmpregadoAssalariado(String id, String nome, String endereco, BigDecimal salarioMensal) {
        super(id, nome, endereco);
        
        this.salarioMensal = salarioMensal.setScale(2, RoundingMode.HALF_EVEN);
    }

    
    @Override
    public String getTipo() {
        return "assalariado";
    }

    
    @Override
    public BigDecimal getSalario() {
        return salarioMensal;
    }

    
    public void setSalarioMensal(BigDecimal salarioMensal) {
        this.salarioMensal = salarioMensal;
    }

    
    @Override
    public Empregado clone() {
        EmpregadoAssalariado clone = new EmpregadoAssalariado(
                this.getId(), this.getNome(), this.getEndereco(), this.getSalario()
        );
        
        super.copiaAtributos(clone);
        return clone;
    }
}