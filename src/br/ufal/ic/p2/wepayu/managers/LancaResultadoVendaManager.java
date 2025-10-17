package br.ufal.ic.p2.wepayu.managers;

import br.ufal.ic.p2.wepayu.ExceptionEmpregados.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.ExceptionVendas.DataInvalidaException;
import br.ufal.ic.p2.wepayu.ExceptionVendas.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.ExceptionVendas.IdentificacaoNulaException;
import br.ufal.ic.p2.wepayu.ExceptionVendas.ValorNaoNumericoException;
import br.ufal.ic.p2.wepayu.ExceptionVendas.ValorNaoPositivoException;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.models.ResultadoVenda;
import br.ufal.ic.p2.wepayu.repository.EmpregadoRepository;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;


public class LancaResultadoVendaManager {
    private final EmpregadoRepository empregadoRepository = EmpregadoRepository.getInstance();
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    
    public void lancaVenda(String id, String data, String valor) throws Exception {
        if (id == null || id.isEmpty()) {
            throw new IdentificacaoNulaException();
        }

        Empregado empregado = empregadoRepository.getById(id);
        if (empregado == null) {
            throw new EmpregadoNaoExisteException();
        }

        if (!(empregado instanceof EmpregadoComissionado)) {
            throw new EmpregadoNaoEhComissionadoException();
        }

        LocalDate dataFormatada;
        try {
            dataFormatada = LocalDate.parse(data, formatter);
        } catch (DateTimeParseException e) {
            throw new DataInvalidaException();
        }

        BigDecimal valorBigDecimal;
        try {
            valorBigDecimal = new BigDecimal(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValorNaoNumericoException();
        }

        if (valorBigDecimal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorNaoPositivoException();
        }

        ((EmpregadoComissionado) empregado).getResultadosVendas().add(new ResultadoVenda(dataFormatada, valorBigDecimal));
        empregadoRepository.salvarDados();
    }

    
    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws Exception {
        Empregado empregado = empregadoRepository.getById(id);
        if (!(empregado instanceof EmpregadoComissionado)) {
            throw new EmpregadoNaoEhComissionadoException();
        }

        LocalDate dataInicio;
        try {
            dataInicio = LocalDate.parse(dataInicial, formatter);
        } catch (DateTimeParseException e) {
            throw new DataInvalidaException("inicial");
        }
        LocalDate dataFim;
        try {
            dataFim = LocalDate.parse(dataFinal, formatter);
        } catch (DateTimeParseException e) {
            throw new DataInvalidaException("final");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new Exception("Data inicial nao pode ser posterior aa data final.");
        }

        BigDecimal totalVendas = BigDecimal.ZERO;
        for (ResultadoVenda venda : ((EmpregadoComissionado) empregado).getResultadosVendas()) {
            if (!venda.getData().isBefore(dataInicio) && venda.getData().isBefore(dataFim)) {
                totalVendas = totalVendas.add(venda.getValor());
            }
        }

        return formatarValor(totalVendas);
    }

    
    private String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "0,00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        DecimalFormat df = new DecimalFormat("0.00", symbols);
        df.setGroupingUsed(false);
        return df.format(valor);
    }
}