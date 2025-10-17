package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class Empregado implements Serializable {
    private String id;
    private String nome;
    private String endereco;
    private boolean sindicalizado;
    private MetodoPagamento metodoPagamento;
    private List<CartaoDePonto> cartoesPonto = new ArrayList<>();
    private String idSindicato;
    private BigDecimal taxaSindical;
    private List<TaxaDeServico> taxasDeServico = new ArrayList<>();
    private LocalDate ultimaDataPagamento;
    private LocalDate dataContratacao;
    private String agendaPagamento;

    
    public Empregado() {
        this.metodoPagamento = new EmMaos();
    }

    
    public Empregado(String id, String nome, String endereco) {
        this();
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.sindicalizado = false;

        
        if (this instanceof EmpregadoHorista) {
            this.dataContratacao = null;
            this.ultimaDataPagamento = null;
            this.agendaPagamento = "semanal 5";
        } else if (this instanceof EmpregadoComissionado) {
            this.dataContratacao = LocalDate.of(2005, 1, 1);
            this.ultimaDataPagamento = LocalDate.of(2004, 12, 31);
            this.agendaPagamento = "semanal 2 5";
        } else { 
            this.dataContratacao = LocalDate.of(2005, 1, 1);
            this.ultimaDataPagamento = LocalDate.of(2004, 12, 31);
            this.agendaPagamento = "mensal $";
        }
    }

    
    public abstract Empregado clone();

    
    protected void copiaAtributos(Empregado clone) {
        clone.setId(this.getId());
        clone.setNome(this.getNome());
        clone.setEndereco(this.getEndereco());
        clone.setSindicalizado(this.isSindicalizado());
        clone.setIdSindicato(this.getIdSindicato());
        clone.setTaxaSindical(this.getTaxaSindical());
        clone.setDataContratacao(this.getDataContratacao());
        clone.setUltimaDataPagamento(this.getUltimaDataPagamento());
        clone.setAgendaPagamento(this.getAgendaPagamento());

        if (this.getMetodoPagamento() != null) {
            clone.setMetodoPagamento(this.getMetodoPagamento().clone());
        }

        if (this.getCartoesPonto() != null) {
            clone.setCartoesPonto(this.getCartoesPonto().stream().map(CartaoDePonto::clone).collect(Collectors.toList()));
        }
        if (this.getTaxasDeServico() != null) {
            clone.setTaxasDeServico(this.getTaxasDeServico().stream().map(TaxaDeServico::clone).collect(Collectors.toList()));
        }
    }

    

    
    public String getId() { return id; }

    
    public String getNome() { return nome; }

    
    public String getEndereco() { return endereco; }

    
    public boolean isSindicalizado() { return sindicalizado; }

    
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }

    
    public abstract String getTipo();

    
    public abstract BigDecimal getSalario();

    
    public void setId(String id) { this.id = id; }

    
    public void setNome(String nome) { this.nome = nome; }

    
    public void setEndereco(String endereco) { this.endereco = endereco; }

    
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }

    
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    
    public List<CartaoDePonto> getCartoesPonto() { return cartoesPonto; }

    
    public void setCartoesPonto(List<CartaoDePonto> cartoesPonto) { this.cartoesPonto = cartoesPonto; }

    
    public String getIdSindicato() { return idSindicato; }

    
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }

    
    public void setTaxaSindical(BigDecimal taxaSindical) { this.taxaSindical = taxaSindical; }

    
    public BigDecimal getTaxaSindical() { return taxaSindical; }

    
    public List<TaxaDeServico> getTaxasDeServico() { return taxasDeServico; }

    
    public void setTaxasDeServico(List<TaxaDeServico> taxasDeServico) { this.taxasDeServico = taxasDeServico; }

    
    public LocalDate getDataContratacao() { return dataContratacao; }

    
    public void setDataContratacao(LocalDate dataContratacao) { this.dataContratacao = dataContratacao; }

    
    public LocalDate getUltimaDataPagamento() { return ultimaDataPagamento; }

    
    public void setUltimaDataPagamento(LocalDate ultimaDataPagamento) { this.ultimaDataPagamento = ultimaDataPagamento; }

    
    public String getAgendaPagamento() { return agendaPagamento; }

    
    public void setAgendaPagamento(String agendaPagamento) { this.agendaPagamento = agendaPagamento; }
}