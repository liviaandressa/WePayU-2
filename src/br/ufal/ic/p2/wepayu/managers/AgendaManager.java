package br.ufal.ic.p2.wepayu.managers;

import br.ufal.ic.p2.wepayu.ExceptionAgenda.AgendaJaExisteException;
import br.ufal.ic.p2.wepayu.ExceptionAgenda.DescricaoAgendaInvalidaException;
import br.ufal.ic.p2.wepayu.utils.XmlUtils;
import java.io.File;
import java.util.HashSet;
import java.util.Set;


public class AgendaManager {
    private final Set<String> agendasDisponiveis = new HashSet<>();
    private final String filename = "agendas.xml";

    
    public AgendaManager() {
        
        agendasDisponiveis.add("semanal 5");
        agendasDisponiveis.add("mensal $");
        agendasDisponiveis.add("semanal 2 5");

        
        carregarDados();
    }

    
    private void carregarDados() {
        Set<String> agendasSalvas = XmlUtils.carregarAgendas(filename);
        agendasDisponiveis.addAll(agendasSalvas);
    }

    
    public void salvarDados() {
        
        Set<String> agendasCustomizadas = new HashSet<>(agendasDisponiveis);
        agendasCustomizadas.remove("semanal 5");
        agendasCustomizadas.remove("mensal $");
        agendasCustomizadas.remove("semanal 2 5");
        XmlUtils.salvarAgendas(filename, agendasCustomizadas);
    }

    
    public void zerarDados() {
        agendasDisponiveis.clear();
        agendasDisponiveis.add("semanal 5");
        agendasDisponiveis.add("mensal $");
        agendasDisponiveis.add("semanal 2 5");

        
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    
    public void criarAgendaDePagamentos(String descricao) throws AgendaJaExisteException, DescricaoAgendaInvalidaException {
        validarDescricao(descricao);
        if (!agendasDisponiveis.add(descricao)) {
            throw new AgendaJaExisteException();
        }
    }

    
    public boolean isAgendaDisponivel(String descricao) {
        return agendasDisponiveis.contains(descricao);
    }

    
    private void validarDescricao(String descricao) throws DescricaoAgendaInvalidaException {
        String[] parts = descricao.split(" ");
        if (parts.length < 1 || parts.length > 3) throw new DescricaoAgendaInvalidaException();

        String tipo = parts[0];

        try {
            if (tipo.equals("mensal")) {
                if (parts.length != 2) throw new DescricaoAgendaInvalidaException();
                if (parts[1].equals("$")) return;
                int dia = Integer.parseInt(parts[1]);
                if (dia < 1 || dia > 28) throw new DescricaoAgendaInvalidaException();
            } else if (tipo.equals("semanal")) {
                if (parts.length == 2) {
                    int dia = Integer.parseInt(parts[1]);
                    if (dia < 1 || dia > 7) throw new DescricaoAgendaInvalidaException();
                } else if (parts.length == 3) {
                    int freq = Integer.parseInt(parts[1]);
                    int dia = Integer.parseInt(parts[2]);
                    if (freq < 1 || freq > 52) throw new DescricaoAgendaInvalidaException();
                    if (dia < 1 || dia > 7) throw new DescricaoAgendaInvalidaException();
                } else {
                    throw new DescricaoAgendaInvalidaException();
                }
            } else {
                throw new DescricaoAgendaInvalidaException();
            }
        } catch (NumberFormatException e) {
            throw new DescricaoAgendaInvalidaException();
        }
    }
}