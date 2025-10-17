package br.ufal.ic.p2.wepayu.repository;

import br.ufal.ic.p2.wepayu.ExceptionSistema.SistemaEncerradoException;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.utils.XmlUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class EmpregadoRepository {
    private static EmpregadoRepository instance;
    protected Map<String, Empregado> data = new LinkedHashMap<>();
    private final String filename = "empregados.xml";
    private boolean sistemaEncerrado = false;

    
    private EmpregadoRepository() {
        carregarDados();
    }

    
    public static synchronized EmpregadoRepository getInstance() {
        if (instance == null) {
            instance = new EmpregadoRepository();
        }
        return instance;
    }

    
    public void verificarSistemaAberto() throws SistemaEncerradoException {
        if (sistemaEncerrado) {
            throw new SistemaEncerradoException();
        }
    }

    
    public void encerrarSistema() {
        salvarDados();
        this.sistemaEncerrado = true;
    }

    
    public void setSistemaEncerrado(boolean status) {
        this.sistemaEncerrado = status;
    }

    
    public static class Memento {
        private final Map<String, Empregado> state;

        
        private Memento(Map<String, Empregado> stateToSave) {
            this.state = stateToSave.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (e1, e2) -> e1, LinkedHashMap::new));
        }

        
        private Map<String, Empregado> getSavedState() {
            return this.state;
        }
    }

    
    public Memento createMemento() {
        return new Memento(this.data);
    }

    
    public void setMemento(Memento memento) {
        this.data = memento.getSavedState().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (e1, e2) -> e1, LinkedHashMap::new));
    }

    
    public void carregarDados() {
        this.data = XmlUtils.carregarDados(filename);
        if (this.data == null) {
            this.data = new LinkedHashMap<>();
        }
    }

    
    public void salvarDados() {
        XmlUtils.salvarDados(filename, this.data);
    }

    
    public void zerarDados() {
        this.data.clear();
        this.sistemaEncerrado = false;
        salvarDados();
    }

    
    public Map<String, Empregado> getAll() {
        return this.data;
    }

    
    public Empregado getById(String id) {
        return this.data.get(id);
    }

    
    public void add(Empregado empregado) {
        String nextId = String.valueOf(getNextId());
        empregado.setId(nextId);
        this.data.put(empregado.getId(), empregado);
    }

    
    public boolean remove(String id) {
        if (this.data.containsKey(id)) {
            this.data.remove(id);
            return true;
        }
        return false;
    }

    
    private int getNextId() {
        if (data.isEmpty()) {
            return 1;
        }
        return data.keySet().stream().mapToInt(Integer::parseInt).max().getAsInt() + 1;
    }
}