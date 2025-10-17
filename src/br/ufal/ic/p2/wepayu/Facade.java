package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.managers.CommandHistoryManager;
import br.ufal.ic.p2.wepayu.managers.FolhaPagamentoManager;
import br.ufal.ic.p2.wepayu.managers.MainManager;
import br.ufal.ic.p2.wepayu.utils.AppUtils;

public class Facade {

    private final MainManager mainManager = new MainManager();
    private final FolhaPagamentoManager folhaPagamentoManager = new FolhaPagamentoManager();
    private final CommandHistoryManager commandHistory = new CommandHistoryManager();

    public Facade() {
        br.ufal.ic.p2.wepayu.repository.EmpregadoRepository.getInstance().setSistemaEncerrado(false);
    }

    public void zerarSistema() throws Exception {
        commandHistory.execute(() -> {
            mainManager.getEmpregadoManager().zerarSistema();
            mainManager.getAgendaManager().zerarDados();
        });
    }

    public void encerrarSistema() {
        mainManager.getAgendaManager().salvarDados();
        mainManager.empregadoManager.empregadoRepository.encerrarSistema();
    }

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        mainManager.getAgendaManager().criarAgendaDePagamentos(descricao);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        return commandHistory.execute(() -> mainManager.getEmpregadoManager().criarEmpregado(nome, endereco, tipo, salario, comissao));
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        return criarEmpregado(nome, endereco, tipo, salario, null);
    }

    public void removerEmpregado(String id) throws Exception {
        commandHistory.execute(() -> mainManager.getEmpregadoManager().removerEmpregado(id));
    }

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        commandHistory.execute(() -> mainManager.getLancaCartaoPontoManager().lancaCartao(emp, data, horas));
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        commandHistory.execute(() -> mainManager.getLancaResultadoVendaManager().lancaVenda(emp, data, valor));
    }

    public void lancaTaxaServico(String idSindicato, String data, String valor) throws Exception {
        commandHistory.execute(() -> mainManager.getLancaTaxaServicoManager().lancaTaxaServico(idSindicato, data, valor));
    }

    public void alteraEmpregado(String emp, String atributo, String valor1) throws Exception {
        commandHistory.execute(() -> mainManager.getEmpregadoManager().alteraEmpregado(emp, atributo, valor1, mainManager.getAgendaManager()));
    }

    public void alteraEmpregado(String emp, String atributo, boolean valor, String idSindicato, String taxaSindical) throws Exception {
        commandHistory.execute(() -> mainManager.getEmpregadoManager().alteraEmpregado(emp, atributo, valor, idSindicato, taxaSindical));
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws Exception {
        commandHistory.execute(() -> mainManager.getEmpregadoManager().alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente));
    }

    public void alteraEmpregado(String emp, String atributo, String tipo, String comissaoOuSalario) throws Exception {
        commandHistory.execute(() -> mainManager.getEmpregadoManager().alteraEmpregado(emp, atributo, tipo, comissaoOuSalario));
    }

    public void rodaFolha(String data, String saida) throws Exception {
        commandHistory.execute(() -> folhaPagamentoManager.rodaFolha(data, saida));
    }

    public void undo() throws Exception {
        commandHistory.undo();
    }

    public void redo() throws Exception {
        commandHistory.redo();
    }

    public String getAtributoEmpregado(String id, String atributo) throws Exception {
        return mainManager.getEmpregadoManager().getAtributoEmpregado(id, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        return mainManager.getEmpregadoManager().getEmpregadoPorNome(nome, indice);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return mainManager.getLancaCartaoPontoManager().getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return mainManager.getLancaCartaoPontoManager().getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return mainManager.getLancaResultadoVendaManager().getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        return mainManager.getLancaTaxaServicoManager().getTaxasServico(emp, dataInicial, dataFinal);
    }

    public String totalFolha(String data) throws Exception {
        return AppUtils.formatBigDecimal(folhaPagamentoManager.totalFolha(data));
    }

    public String getNumeroDeEmpregados() {
        return String.valueOf(mainManager.getEmpregadoManager().getNumeroDeEmpregados());
    }
}
