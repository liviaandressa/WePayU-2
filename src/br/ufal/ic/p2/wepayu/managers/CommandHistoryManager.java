package br.ufal.ic.p2.wepayu.managers;

import br.ufal.ic.p2.wepayu.repository.EmpregadoRepository;
import java.util.Stack;
import java.util.concurrent.Callable;


public class CommandHistoryManager {
    private final Stack<EmpregadoRepository.Memento> undoStack = new Stack<>();
    private final Stack<EmpregadoRepository.Memento> redoStack = new Stack<>();
    private final EmpregadoRepository empregadoRepository = EmpregadoRepository.getInstance();

    
    @FunctionalInterface
    public interface Command {
        void execute() throws Exception;
    }

    
    public void execute(Command command) throws Exception {
        empregadoRepository.verificarSistemaAberto(); 
        EmpregadoRepository.Memento beforeState = empregadoRepository.createMemento();
        try {
            command.execute();
            undoStack.push(beforeState);
            redoStack.clear(); 
        } catch (Exception e) {
            empregadoRepository.setMemento(beforeState); 
            throw e;
        }
    }

    
    public <T> T execute(Callable<T> command) throws Exception {
        empregadoRepository.verificarSistemaAberto(); 
        EmpregadoRepository.Memento beforeState = empregadoRepository.createMemento();
        try {
            T result = command.call();
            undoStack.push(beforeState);
            redoStack.clear(); 
            return result;
        } catch (Exception e) {
            empregadoRepository.setMemento(beforeState); 
            throw e;
        }
    }

    
    public void undo() throws Exception {
        empregadoRepository.verificarSistemaAberto(); 
        if (undoStack.isEmpty()) {
            throw new Exception("Nao ha comando a desfazer.");
        }
        redoStack.push(empregadoRepository.createMemento());
        EmpregadoRepository.Memento previousState = undoStack.pop();
        empregadoRepository.setMemento(previousState);
    }

    
    public void redo() throws Exception {
        empregadoRepository.verificarSistemaAberto(); 
        if (redoStack.isEmpty()) {
            throw new Exception("Nao ha comando a refazer.");
        }
        undoStack.push(empregadoRepository.createMemento());
        EmpregadoRepository.Memento nextState = redoStack.pop();
        empregadoRepository.setMemento(nextState);
    }
}