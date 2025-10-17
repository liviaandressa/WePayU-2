package br.ufal.ic.p2.wepayu.utils;

import br.ufal.ic.p2.wepayu.models.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class XmlUtils {

    
    public static Map<String, Empregado> carregarDados(String filename) {
        Map<String, Empregado> empregados = new LinkedHashMap<>();
        try {
            File file = new File(filename);
            if (!file.exists()) return empregados;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("empregado");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                Empregado empregado = parseEmpregado(element);
                if (empregado != null) {
                    empregados.put(empregado.getId(), empregado);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return empregados;
    }

    
    private static Empregado parseEmpregado(Element element) {

        String id = element.getAttribute("id");
        String tipo = element.getAttribute("tipo");
        String nome = getElementText(element, "nome");
        String endereco = getElementText(element, "endereco");
        String sindicalizado = getElementText(element, "sindicalizado");
        String idSindicato = getElementText(element, "idSindicato");
        String taxaSindical = getElementText(element, "taxaSindical");
        String agendaPagamento = getElementText(element, "agendaPagamento");


        Empregado empregado;
        switch (tipo) {
            case "horista":
                String salarioHora = getElementText(element, "salarioHora");
                empregado = new EmpregadoHorista(id, nome, endereco, new BigDecimal(salarioHora));
                break;
            case "assalariado":
                String salarioMensal = getElementText(element, "salarioMensal");
                empregado = new EmpregadoAssalariado(id, nome, endereco, new BigDecimal(salarioMensal));
                break;
            case "comissionado":
                String salarioBase = getElementText(element, "salarioMensal");
                String comissao = getElementText(element, "comissao");
                empregado = new EmpregadoComissionado(id, nome, endereco,
                        new BigDecimal(salarioBase), new BigDecimal(comissao));
                break;
            default:
                return null;
        }

        empregado.setSindicalizado(Boolean.parseBoolean(sindicalizado));
        empregado.setIdSindicato(idSindicato);
        if (taxaSindical != null) {
            empregado.setTaxaSindical(new BigDecimal(taxaSindical));
        }

        if (agendaPagamento != null && !agendaPagamento.isEmpty()) {
            empregado.setAgendaPagamento(agendaPagamento);
        }

        String metodoPagamento = getElementText(element, "metodoPagamento");
        if (metodoPagamento != null) {
            switch (metodoPagamento) {
                case "emMaos":
                    empregado.setMetodoPagamento(new EmMaos());
                    break;
                case "correios":
                    empregado.setMetodoPagamento(new Correios());
                    break;
                case "banco":
                    String banco = getElementText(element, "banco");
                    String agencia = getElementText(element, "agencia");
                    String conta = getElementText(element, "contaCorrente");
                    empregado.setMetodoPagamento(new Banco(banco, agencia, conta));
                    break;
            }
        }

        NodeList cartoes = element.getElementsByTagName("cartao");
        for (int i = 0; i < cartoes.getLength(); i++) {
            Element cartaoElement = (Element) cartoes.item(i);
            String dataStr = getElementText(cartaoElement, "data");
            String horasStr = getElementText(cartaoElement, "horas");
            LocalDate data = LocalDate.parse(dataStr);
            BigDecimal horas = new BigDecimal(horasStr);
            empregado.getCartoesPonto().add(new CartaoDePonto(data, horas));
        }

        NodeList taxas = element.getElementsByTagName("taxa");
        for (int i = 0; i < taxas.getLength(); i++) {
            Element taxaElement = (Element) taxas.item(i);
            String dataStr = getElementText(taxaElement, "data");
            String valorStr = getElementText(taxaElement, "valor");
            LocalDate data = LocalDate.parse(dataStr);
            BigDecimal valor = new BigDecimal(valorStr);
            empregado.getTaxasDeServico().add(new TaxaDeServico(data, valor));
        }

        if (empregado instanceof EmpregadoComissionado) {
            NodeList vendas = element.getElementsByTagName("venda");
            for (int i = 0; i < vendas.getLength(); i++) {
                Element vendaElement = (Element) vendas.item(i);
                String dataStr = getElementText(vendaElement, "data");
                String valorStr = getElementText(vendaElement, "valor");
                LocalDate data = LocalDate.parse(dataStr);
                BigDecimal valor = new BigDecimal(valorStr);
                ((EmpregadoComissionado) empregado).getResultadosVendas().add(new ResultadoVenda(data, valor));
            }
        }

        String ultimaDataPagamentoStr = getElementText(element, "ultimaDataPagamento");
        if (ultimaDataPagamentoStr != null) {
            empregado.setUltimaDataPagamento(LocalDate.parse(ultimaDataPagamentoStr));
        }

        return empregado;
    }

    
    public static void salvarDados(String filename, Map<String, Empregado> data) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("empregados");
            doc.appendChild(root);

            for (Empregado empregado : data.values()) {
                Element empElement = doc.createElement("empregado");
                empElement.setAttribute("id", empregado.getId());
                empElement.setAttribute("tipo", empregado.getTipo());

                addElement(doc, empElement, "nome", empregado.getNome());
                addElement(doc, empElement, "endereco", empregado.getEndereco());
                addElement(doc, empElement, "agendaPagamento", empregado.getAgendaPagamento());
                addElement(doc, empElement, "sindicalizado", String.valueOf(empregado.isSindicalizado()));
                addElement(doc, empElement, "idSindicato", empregado.getIdSindicato());
                if (empregado.getTaxaSindical() != null) {
                    addElement(doc, empElement, "taxaSindical", empregado.getTaxaSindical().toString());
                }

                if (empregado instanceof EmpregadoHorista) {
                    addElement(doc, empElement, "salarioHora", empregado.getSalario().toString());
                } else {
                    addElement(doc, empElement, "salarioMensal", empregado.getSalario().toString());
                }

                if (empregado instanceof EmpregadoComissionado) {
                    addElement(doc, empElement, "comissao",
                            ((EmpregadoComissionado) empregado).getComissao().toString());
                }

                MetodoPagamento metodo = empregado.getMetodoPagamento();
                if (metodo instanceof EmMaos) {
                    addElement(doc, empElement, "metodoPagamento", "emMaos");
                } else if (metodo instanceof Correios) {
                    addElement(doc, empElement, "metodoPagamento", "correios");
                } else if (metodo instanceof Banco) {
                    addElement(doc, empElement, "metodoPagamento", "banco");
                    Banco banco = (Banco) metodo;
                    addElement(doc, empElement, "banco", banco.getBanco());
                    addElement(doc, empElement, "agencia", banco.getAgencia());
                    addElement(doc, empElement, "contaCorrente", banco.getContaCorrente());
                }

                for (CartaoDePonto cartao : empregado.getCartoesPonto()) {
                    Element cartaoElement = doc.createElement("cartao");
                    addElement(doc, cartaoElement, "data", cartao.getData().toString());
                    addElement(doc, cartaoElement, "horas", cartao.getHoras().toString());
                    empElement.appendChild(cartaoElement);
                }

                for (TaxaDeServico taxa : empregado.getTaxasDeServico()) {
                    Element taxaElement = doc.createElement("taxa");
                    addElement(doc, taxaElement, "data", taxa.getData().toString());
                    addElement(doc, taxaElement, "valor", taxa.getValor().toString());
                    empElement.appendChild(taxaElement);
                }

                if (empregado instanceof EmpregadoComissionado) {
                    for (ResultadoVenda venda : ((EmpregadoComissionado) empregado).getResultadosVendas()) {
                        Element vendaElement = doc.createElement("venda");
                        addElement(doc, vendaElement, "data", venda.getData().toString());
                        addElement(doc, vendaElement, "valor", venda.getValor().toString());
                        empElement.appendChild(vendaElement);
                    }
                }

                if (empregado.getUltimaDataPagamento() != null) {
                    addElement(doc, empElement, "ultimaDataPagamento", empregado.getUltimaDataPagamento().toString());
                }

                root.appendChild(empElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filename));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static void salvarAgendas(String filename, Set<String> data) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("agendas");
            doc.appendChild(root);

            for (String descricao : data) {
                addElement(doc, root, "agenda", descricao);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filename));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static Set<String> carregarAgendas(String filename) {
        Set<String> agendas = new HashSet<>();
        try {
            File file = new File(filename);
            if (!file.exists()) return agendas;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("agenda");
            for (int i = 0; i < nodeList.getLength(); i++) {
                agendas.add(nodeList.item(i).getTextContent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agendas;
    }


    
    private static String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    
    private static void addElement(Document doc, Element parent, String tagName, String value) {
        if (value != null) {
            Element element = doc.createElement(tagName);
            element.setTextContent(value);
            parent.appendChild(element);
        }
    }
}