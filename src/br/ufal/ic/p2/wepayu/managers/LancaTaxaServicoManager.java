package br.ufal.ic.p2.wepayu.managers;

import br.ufal.ic.p2.wepayu.ExceptionEmpregados.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.ExceptionServico.*;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.TaxaDeServico;
import br.ufal.ic.p2.wepayu.repository.EmpregadoRepository;
import br.ufal.ic.p2.wepayu.utils.AppUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;


public class LancaTaxaServicoManager {
    private final EmpregadoRepository empregadoRepository = EmpregadoRepository.getInstance();

    
    private Empregado encontrarPorIdSindicato(String idSindicato) {
        for (Empregado empregado : empregadoRepository.getAll().values()) {
            if (Objects.equals(empregado.getIdSindicato(), idSindicato)) {
                return empregado;
            }
        }
        return null;
    }

    
    public void lancaTaxaServico(String idSindicato, String data, String valor) throws Exception {
        if (idSindicato == null || idSindicato.isEmpty()) {
            throw new IdentificacaoNulaException();
        }

        Empregado empregado = encontrarPorIdSindicato(idSindicato);
        if (empregado == null) {
            throw new MembroNaoExisteException();
        }

        if (!empregado.isSindicalizado()) {
            throw new EmpregadoNaoEhSindicalizadoException();
        }

        LocalDate dataFormatada;
        try {
            dataFormatada = AppUtils.parseDate(data);
        } catch (DateTimeParseException e) {
            throw new DataInvalidaException();
        }

        BigDecimal valorBigDecimal;
        try {
            valorBigDecimal = AppUtils.parseBigDecimal(valor);
        } catch (NumberFormatException e) {
            throw new ValorNaoNumericoException();
        }

        if (valorBigDecimal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorNaoPositivoException();
        }

        empregado.getTaxasDeServico().add(new TaxaDeServico(dataFormatada, valorBigDecimal));
        empregadoRepository.salvarDados();
    }

    
    public String getTaxasServico(String id, String dataInicial, String dataFinal) throws Exception {
        Empregado empregado = empregadoRepository.getById(id);
        if (empregado == null) {
            throw new EmpregadoNaoExisteException();
        }
        if (!empregado.isSindicalizado()) {
            throw new EmpregadoNaoEhSindicalizadoException();
        }

        LocalDate dataInicio;
        try {
            dataInicio = AppUtils.parseDate(dataInicial);
        } catch (DateTimeParseException e) {
            throw new DataInvalidaException("inicial");
        }
        LocalDate dataFim;
        try {
            dataFim = AppUtils.parseDate(dataFinal);
        } catch (DateTimeParseException e) {
            throw new DataInvalidaException("final");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new Exception("Data inicial nao pode ser posterior aa data final.");
        }

        BigDecimal totalTaxas = BigDecimal.ZERO;
        for (TaxaDeServico taxa : empregado.getTaxasDeServico()) {
            if (!taxa.getData().isBefore(dataInicio) && taxa.getData().isBefore(dataFim)) {
                totalTaxas = totalTaxas.add(taxa.getValor());
            }
        }

        return AppUtils.formatBigDecimal(totalTaxas);
    }
}