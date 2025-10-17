package br.ufal.ic.p2.wepayu.managers;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.repository.EmpregadoRepository;
import br.ufal.ic.p2.wepayu.utils.AppUtils;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FolhaPagamentoManager {

    private final EmpregadoRepository empregadoRepository = EmpregadoRepository.getInstance();

    public BigDecimal totalFolha(String data) throws Exception {
        LocalDate dataAtual = AppUtils.parseDate(data);
        BigDecimal total = BigDecimal.ZERO;

        for (Empregado emp : empregadoRepository.getAll().values()) {
            if (deveSerPago(emp, dataAtual)) {
                total = total.add(calcularPagamento(emp, dataAtual));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public void rodaFolha(String data, String saida) throws Exception {
        LocalDate dataAtual = AppUtils.parseDate(data);
        List<Empregado> empregadosParaPagar = empregadoRepository.getAll().values().stream()
                .filter(emp -> deveSerPago(emp, dataAtual))
                .sorted(Comparator.comparing(Empregado::getNome))
                .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter(new FileWriter(saida))) {
            writer.println("FOLHA DE PAGAMENTO DO DIA " + dataAtual);
            writer.println("====================================");
            writer.println();

            BigDecimal totalGeral = BigDecimal.ZERO;
            totalGeral = totalGeral.add(gerarRelatorio(writer, empregadosParaPagar, "horista", dataAtual));
            totalGeral = totalGeral.add(gerarRelatorio(writer, empregadosParaPagar, "assalariado", dataAtual));
            totalGeral = totalGeral.add(gerarRelatorio(writer, empregadosParaPagar, "comissionado", dataAtual));

            writer.printf(Locale.FRANCE, "TOTAL FOLHA: %.2f\n", totalGeral);
        }

        for (Empregado emp : empregadosParaPagar) {
            emp.setUltimaDataPagamento(dataAtual);
        }
    }

    private BigDecimal gerarRelatorio(PrintWriter writer, List<Empregado> empregados, String tipo, LocalDate dataAtual) {
        BigDecimal totalBruto = BigDecimal.ZERO, totalDescontos = BigDecimal.ZERO, totalLiquido = BigDecimal.ZERO;
        BigDecimal totalHorasNormais = BigDecimal.ZERO, totalHorasExtras = BigDecimal.ZERO;
        BigDecimal totalFixo = BigDecimal.ZERO, totalVendas = BigDecimal.ZERO, totalComissao = BigDecimal.ZERO;

        List<Empregado> filtrados = empregados.stream().filter(e -> e.getTipo().equals(tipo)).collect(Collectors.toList());

        if (tipo.equals("horista")) {
            writer.println("===============================================================================================================================");
            writer.println("===================== HORISTAS ================================================================================================");
            writer.println("===============================================================================================================================");
            writer.printf("%-36s %5s %5s %13s %9s %15s %s\n", "Nome", "Horas", "Extra", "Salario Bruto", "Descontos", "Salario Liquido", "Metodo");
            writer.println("==================================== ===== ===== ============= ========= =============== ======================================");
        } else if (tipo.equals("assalariado")) {
            writer.println("===============================================================================================================================");
            writer.println("===================== ASSALARIADOS ============================================================================================");
            writer.println("===============================================================================================================================");
            writer.printf("%-48s %13s %9s %15s %s\n", "Nome", "Salario Bruto", "Descontos", "Salario Liquido", "Metodo");
            writer.println("================================================ ============= ========= =============== ======================================");
        } else {
            writer.println("===============================================================================================================================");
            writer.println("===================== COMISSIONADOS ===========================================================================================");
            writer.println("===============================================================================================================================");
            writer.printf("%-17s %8s %10s %10s %13s %9s %15s %s\n", "Nome", "Fixo", "Vendas", "Comissao", "Salario Bruto", "Descontos", "Salario Liquido", "Metodo");
            writer.println("===================== ======== ======== ======== ============= ========= =============== ======================================");
        }

        for (Empregado emp : filtrados) {
            PagamentoInfo info;
            if (deveSerPago(emp, dataAtual)) {
                info = calcularPagamentoCompleto(emp, dataAtual);
            } else {
                info = new PagamentoInfo();
            }
            writer.print(formatarLinhaRelatorio(emp, info));

            totalBruto = totalBruto.add(info.salarioBruto);
            totalDescontos = totalDescontos.add(info.descontos);
            totalLiquido = totalLiquido.add(info.salarioLiquido);

            if (emp instanceof EmpregadoHorista) {
                totalHorasNormais = totalHorasNormais.add(info.horasNormais);
                totalHorasExtras = totalHorasExtras.add(info.horasExtras);
            } else if (emp instanceof EmpregadoComissionado) {
                totalFixo = totalFixo.add(info.salarioFixo);
                totalVendas = totalVendas.add(info.vendas);
                totalComissao = totalComissao.add(info.comissao);
            }
        }
        writer.println();

        if (tipo.equals("horista")) {
            writer.print("TOTAL HORISTAS ");
            writer.printf("%27s %5s %13s %9s %15s\n",
                    totalHorasNormais.toPlainString(), totalHorasExtras.toPlainString(),
                    AppUtils.formatBigDecimal(totalBruto), AppUtils.formatBigDecimal(totalDescontos), AppUtils.formatBigDecimal(totalLiquido));
        } else if (tipo.equals("assalariado")) {
            writer.print("TOTAL ASSALARIADOS ");
            writer.printf("%43s %9s %15s\n",
                    AppUtils.formatBigDecimal(totalBruto), AppUtils.formatBigDecimal(totalDescontos), AppUtils.formatBigDecimal(totalLiquido));
        } else {
            writer.print("TOTAL COMISSIONADOS ");
            writer.printf("%10s %8s %8s %13s %9s %15s\n",
                    AppUtils.formatBigDecimal(totalFixo), AppUtils.formatBigDecimal(totalVendas), AppUtils.formatBigDecimal(totalComissao),
                    AppUtils.formatBigDecimal(totalBruto), AppUtils.formatBigDecimal(totalDescontos), AppUtils.formatBigDecimal(totalLiquido));
        }
        writer.println();
        return totalBruto;
    }

    private String getMetodoPagamentoString(Empregado emp) {
        MetodoPagamento metodo = emp.getMetodoPagamento();
        if (metodo instanceof EmMaos) {
            return "Em maos";
        }
        if (metodo instanceof Correios) {
            return "Correios, " + emp.getEndereco();
        }
        if (metodo instanceof Banco banco) {
            return banco.getBanco() + ", Ag. " + banco.getAgencia() + " CC " + banco.getContaCorrente();
        }
        return "";
    }

    private boolean deveSerPago(Empregado emp, LocalDate data) {
        LocalDate dataContratacao = emp.getDataContratacao();
        if (dataContratacao != null && data.isBefore(dataContratacao)) {
            return false;
        }

        String agenda = emp.getAgendaPagamento();
        String[] parts = agenda.split(" ");
        String tipoAgenda = parts[0];

        if (tipoAgenda.equals("mensal")) {
            String dia = parts[1];
            if (dia.equals("$")) {
                LocalDate ultimoDiaUtil = data.with(TemporalAdjusters.lastDayOfMonth());
                while (ultimoDiaUtil.getDayOfWeek() == DayOfWeek.SATURDAY || ultimoDiaUtil.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    ultimoDiaUtil = ultimoDiaUtil.minusDays(1);
                }
                return data.equals(ultimoDiaUtil);
            } else {
                int diaMes = Integer.parseInt(dia);
                if (data.getDayOfMonth() == diaMes) {
                    return true;
                }
                LocalDate ultimoDiaDoMes = data.with(TemporalAdjusters.lastDayOfMonth());
                if (diaMes > ultimoDiaDoMes.getDayOfMonth() && data.equals(ultimoDiaDoMes)) {
                    return true;
                }
                return false;
            }
        } else if (tipoAgenda.equals("semanal")) {
            int frequencia, diaDaSemana;

            if (parts.length == 3) {
                frequencia = Integer.parseInt(parts[1]);
                diaDaSemana = Integer.parseInt(parts[2]);
            } else {
                frequencia = 1;
                diaDaSemana = Integer.parseInt(parts[1]);
            }

            if (data.getDayOfWeek().getValue() != diaDaSemana) {
                return false;
            }

            LocalDate anchorDate = LocalDate.of(2004, 12, 27);
            long weeksSinceAnchor = ChronoUnit.WEEKS.between(anchorDate, data);

            return weeksSinceAnchor >= 0 && weeksSinceAnchor % frequencia == 0;
        }
        return false;
    }

    private BigDecimal calcularPagamento(Empregado emp, LocalDate data) {
        return calcularPagamentoCompleto(emp, data).salarioBruto;
    }

    private PagamentoInfo calcularPagamentoCompleto(Empregado emp, LocalDate data) {
        return switch (emp.getTipo()) {
            case "horista" ->
                calcularPagamentoHoristaCompleto((EmpregadoHorista) emp, data);
            case "assalariado" ->
                calcularPagamentoAssalariadoCompleto((EmpregadoAssalariado) emp, data);
            case "comissionado" ->
                calcularPagamentoComissionadoCompleto((EmpregadoComissionado) emp, data);
            default ->
                new PagamentoInfo();
        };
    }

    private LocalDate obterInicioPeriodo(Empregado emp, LocalDate dataPagamento) {
        LocalDate inicioCalculado;
        String agenda = emp.getAgendaPagamento();

        String[] parts = agenda.split(" ");
        if (parts[0].equals("semanal")) {
            int frequencia = (parts.length == 2) ? 1 : Integer.parseInt(parts[1]);
            inicioCalculado = dataPagamento.minusWeeks(frequencia).plusDays(1);
        } else {
            inicioCalculado = dataPagamento.withDayOfMonth(1);
        }

        LocalDate inicioAposUltimoPagamento = emp.getUltimaDataPagamento() != null ? emp.getUltimaDataPagamento().plusDays(1) : null;

        if (inicioAposUltimoPagamento != null && inicioAposUltimoPagamento.isAfter(inicioCalculado)) {
            return inicioAposUltimoPagamento;
        }

        return inicioCalculado;
    }

    private PagamentoInfo calcularPagamentoHoristaCompleto(EmpregadoHorista horista, LocalDate data) {
        PagamentoInfo info = new PagamentoInfo();
        LocalDate inicioPeriodo = obterInicioPeriodo(horista, data);

        for (CartaoDePonto cartao : horista.getCartoesPonto()) {
            LocalDate dataCartao = cartao.getData();
            if (!dataCartao.isBefore(inicioPeriodo) && !dataCartao.isAfter(data)) {
                BigDecimal horas = cartao.getHoras();
                if (horas.compareTo(new BigDecimal("8")) > 0) {
                    info.horasNormais = info.horasNormais.add(new BigDecimal("8"));
                    info.horasExtras = info.horasExtras.add(horas.subtract(new BigDecimal("8")));
                } else {
                    info.horasNormais = info.horasNormais.add(horas);
                }
            }
        }

        BigDecimal salarioHora = horista.getSalario();
        info.salarioBruto = info.horasNormais.multiply(salarioHora)
                .add(info.horasExtras.multiply(salarioHora.multiply(new BigDecimal("1.5"))));

        info.descontos = calcularDescontosSindicais(horista, inicioPeriodo, data);
        info.salarioLiquido = info.salarioBruto.subtract(info.descontos);
        if (info.salarioLiquido.compareTo(BigDecimal.ZERO) < 0) {
            info.salarioLiquido = BigDecimal.ZERO;
        }

        return info;
    }

    private PagamentoInfo calcularPagamentoAssalariadoCompleto(EmpregadoAssalariado assalariado, LocalDate data) {
        PagamentoInfo info = new PagamentoInfo();
        String agenda = assalariado.getAgendaPagamento();
        BigDecimal salarioMensal = assalariado.getSalario();

        if (agenda.startsWith("semanal")) {
            String[] parts = agenda.split(" ");
            int frequencia = (parts.length == 3) ? Integer.parseInt(parts[1]) : 1;
            info.salarioBruto = salarioMensal.multiply(new BigDecimal("12"))
                    .multiply(new BigDecimal(frequencia))
                    .divide(new BigDecimal("52"), 2, RoundingMode.DOWN);
        } else {
            info.salarioBruto = salarioMensal;
        }

        LocalDate inicioPeriodo = obterInicioPeriodo(assalariado, data);
        info.descontos = calcularDescontosSindicais(assalariado, inicioPeriodo, data);
        info.salarioLiquido = info.salarioBruto.subtract(info.descontos);
        if (info.salarioLiquido.compareTo(BigDecimal.ZERO) < 0) {
            info.salarioLiquido = BigDecimal.ZERO;
        }
        return info;
    }

    private PagamentoInfo calcularPagamentoComissionadoCompleto(EmpregadoComissionado comissionado, LocalDate data) {
        PagamentoInfo info = new PagamentoInfo();
        LocalDate inicioPeriodo = obterInicioPeriodo(comissionado, data);

        String agenda = comissionado.getAgendaPagamento();
        BigDecimal salarioMensal = comissionado.getSalario();

        if (agenda.startsWith("semanal")) {
            String[] parts = agenda.split(" ");
            int frequencia = (parts.length == 3) ? Integer.parseInt(parts[1]) : 1;
            info.salarioFixo = salarioMensal.multiply(new BigDecimal("12"))
                    .multiply(new BigDecimal(frequencia))
                    .divide(new BigDecimal("52"), 2, RoundingMode.DOWN);
        } else {
            info.salarioFixo = salarioMensal;
        }

        for (ResultadoVenda venda : comissionado.getResultadosVendas()) {
            LocalDate dataVenda = venda.getData();
            if (!dataVenda.isBefore(inicioPeriodo) && !dataVenda.isAfter(data)) {
                info.vendas = info.vendas.add(venda.getValor());
            }
        }

        info.comissao = info.vendas.multiply(comissionado.getComissao()).setScale(2, RoundingMode.DOWN);
        info.salarioBruto = info.salarioFixo.add(info.comissao);
        info.descontos = calcularDescontosSindicais(comissionado, inicioPeriodo, data);
        info.salarioLiquido = info.salarioBruto.subtract(info.descontos);
        if (info.salarioLiquido.compareTo(BigDecimal.ZERO) < 0) {
            info.salarioLiquido = BigDecimal.ZERO;
        }

        return info;
    }

    private BigDecimal calcularDescontosSindicais(Empregado emp, LocalDate inicio, LocalDate fim) {
        BigDecimal descontos = BigDecimal.ZERO;
        if (emp.isSindicalizado() && emp.getTaxaSindical() != null && inicio != null) {

            String agenda = emp.getAgendaPagamento();

            long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fim) + 1;
            if (dias < 0) {
                dias = 0;
            }
            descontos = descontos.add(emp.getTaxaSindical().multiply(new BigDecimal(dias)));

            for (TaxaDeServico taxa : emp.getTaxasDeServico()) {
                if (!taxa.getData().isBefore(inicio) && !taxa.getData().isAfter(fim)) {
                    descontos = descontos.add(taxa.getValor());
                }
            }
        }

        return descontos;
    }

    private String formatarLinhaRelatorio(Empregado emp, PagamentoInfo info) {
        String metodo = getMetodoPagamentoString(emp);
        if (emp instanceof EmpregadoHorista) {
            String horasNormaisStr = info.horasNormais.toPlainString();
            String horasExtrasStr = info.horasExtras.toPlainString();
            return String.format("%-36s %5s %5s %13s %9s %15s %s\n",
                    emp.getNome(), horasNormaisStr, horasExtrasStr, AppUtils.formatBigDecimal(info.salarioBruto),
                    AppUtils.formatBigDecimal(info.descontos), AppUtils.formatBigDecimal(info.salarioLiquido), metodo);
        } else if (emp instanceof EmpregadoComissionado) {
            return String.format("%-17s %8s %10s %10s %13s %9s %15s %s\n",
                    emp.getNome(), AppUtils.formatBigDecimal(info.salarioFixo), AppUtils.formatBigDecimal(info.vendas),
                    AppUtils.formatBigDecimal(info.comissao), AppUtils.formatBigDecimal(info.salarioBruto),
                    AppUtils.formatBigDecimal(info.descontos), AppUtils.formatBigDecimal(info.salarioLiquido), metodo);
        } else {
            return String.format("%-48s %13s %9s %15s %s\n",
                    emp.getNome(), AppUtils.formatBigDecimal(info.salarioBruto), AppUtils.formatBigDecimal(info.descontos),
                    AppUtils.formatBigDecimal(info.salarioLiquido), metodo);
        }
    }

    private class PagamentoInfo {

        BigDecimal salarioBruto = BigDecimal.ZERO, descontos = BigDecimal.ZERO, salarioLiquido = BigDecimal.ZERO;
        BigDecimal horasNormais = BigDecimal.ZERO, horasExtras = BigDecimal.ZERO;
        BigDecimal salarioFixo = BigDecimal.ZERO, vendas = BigDecimal.ZERO, comissao = BigDecimal.ZERO;
    }
}
