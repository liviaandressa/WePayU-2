package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class CartaoDePonto implements Serializable {
    private String dataStr;
    private String horasStr;

    
    public CartaoDePonto() {}

    
    public CartaoDePonto(LocalDate data, BigDecimal horas) {
        setData(data);
        setHoras(horas);
    }

    
    @Override
    public CartaoDePonto clone() {
        return new CartaoDePonto(this.getData(), this.getHoras());
    }

    
    public LocalDate getData() { if (dataStr == null) return null; return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu")); }

    
    public void setData(LocalDate data) { if (data != null) { this.dataStr = data.format(DateTimeFormatter.ofPattern("d/M/uuuu")); } else { this.dataStr = null; } }

    
    public BigDecimal getHoras() { if (horasStr == null) return null; return new BigDecimal(horasStr.replace(",", ".")); }

    
    public void setHoras(BigDecimal horas) { if (horas != null) { this.horasStr = horas.toPlainString(); } else { this.horasStr = null; } }

    
    public String getDataStr() { return dataStr; }

    
    public void setDataStr(String dataStr) { this.dataStr = dataStr; }

    
    public String getHorasStr() { return horasStr; }

    
    public void setHorasStr(String horasStr) { this.horasStr = horasStr; }
}