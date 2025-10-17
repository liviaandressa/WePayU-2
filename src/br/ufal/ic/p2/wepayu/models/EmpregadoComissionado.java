package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class EmpregadoComissionado extends Empregado {
    private BigDecimal salarioMensal;
    private BigDecimal comissao;
    private List<ResultadoVenda> resultadosVendas = new ArrayList<>();

    
    public EmpregadoComissionado() {
        super();
    }

    
    public EmpregadoComissionado(String id, String nome, String endereco, BigDecimal salarioMensal, BigDecimal comissao) {
        super(id, nome, endereco);
        this.salarioMensal = salarioMensal.setScale(2, RoundingMode.HALF_EVEN);
        this.comissao = comissao.setScale(2, RoundingMode.HALF_EVEN);
    }

    
    @Override
    public Empregado clone() {
        EmpregadoComissionado clone = new EmpregadoComissionado(
                this.getId(), this.getNome(), this.getEndereco(), this.getSalario(), this.getComissao()
        );
        
        super.copiaAtributos(clone);

        
        if (this.getResultadosVendas() != null) {
            clone.setResultadosVendas(
                    this.getResultadosVendas().stream()
                            .map(ResultadoVenda::clone)
                            .collect(Collectors.toList())
            );
        }
        return clone;
    }

    
    @Override
    public String getTipo() {
        return "comissionado";
    }

    
    @Override
    public BigDecimal getSalario() {
        return salarioMensal;
    }

    
    public BigDecimal getComissao() {
        return comissao;
    }

    
    public void setComissao(BigDecimal comissao) {
        this.comissao = comissao.setScale(2, RoundingMode.HALF_EVEN);
    }

    
    public void setSalarioMensal(BigDecimal salarioMensal) {
        this.salarioMensal = salarioMensal;
    }

    
    public List<ResultadoVenda> getResultadosVendas() {
        return resultadosVendas;
    }

    
    public void setResultadosVendas(List<ResultadoVenda> resultadosVendas) {
        this.resultadosVendas = resultadosVendas;
    }
}