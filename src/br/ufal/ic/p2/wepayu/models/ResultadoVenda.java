package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ResultadoVenda implements Serializable {
    private String dataStr;
    private String valorStr;

    
    public ResultadoVenda() {}

    
    public ResultadoVenda(LocalDate data, BigDecimal valor) {
        setData(data);
        setValor(valor);
    }

    
    public LocalDate getData() {
        if (dataStr == null) return null;
        return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu"));
    }

    
    public void setData(LocalDate data) {
        if (data != null) {
            this.dataStr = data.format(DateTimeFormatter.ofPattern("d/M/uuuu"));
        } else {
            this.dataStr = null;
        }
    }

    
    public BigDecimal getValor() {
        if (valorStr == null) return null;
        return new BigDecimal(valorStr.replace(",", "."));
    }

    
    public void setValor(BigDecimal valor) {
        if (valor != null) {
            this.valorStr = valor.toPlainString();
        } else {
            this.valorStr = null;
        }
    }

    
    public String getDataStr() {
        return dataStr;
    }

    
    public void setDataStr(String dataStr) {
        this.dataStr = dataStr;
    }

    
    public String getValorStr() {
        return valorStr;
    }

    
    public void setValorStr(String valorStr) {
        this.valorStr = valorStr;
    }

    
    @Override
    public ResultadoVenda clone() {
        return new ResultadoVenda(this.getData(), this.getValor());
    }
}