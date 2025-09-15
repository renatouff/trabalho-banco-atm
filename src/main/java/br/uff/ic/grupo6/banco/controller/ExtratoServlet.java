package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.TransacaoDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Transacao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;

public class ExtratoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

        if (clienteLogado == null || !(clienteLogado instanceof Cliente)) {
            response.sendRedirect("login.jsp?erro=Acesso não autorizado. Faça login como cliente.");
            return;
        }

        int idConta = clienteLogado.getConta().getId();
        LocalDate dataInicio = null;
        LocalDate dataFim = null;

        // Tenta obter e parsear as datas dos parâmetros da requisição
        String dataInicioStr = request.getParameter("dataInicio");
        String dataFimStr = request.getParameter("dataFim");

        try {
            if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
                dataInicio = LocalDate.parse(dataInicioStr);
            }
            if (dataFimStr != null && !dataFimStr.isEmpty()) {
                dataFim = LocalDate.parse(dataFimStr);
            }
        } catch (DateTimeParseException e) {
            request.setAttribute("erro", "Formato de data inválido. Use AAAA-MM-DD.");
            // Continua para a busca, mas com datas nulas, ou redireciona de volta?
            // Para simplicidade, vamos prosseguir com datas nulas, mas o ideal seria voltar.
            // response.sendRedirect("extrato.jsp?erro=Formato de data inválido.");
            // return;
        }

        TransacaoDAO transacaoDAO = new TransacaoDAO();
        List<Transacao> transacoes = new ArrayList<>();

        try {
            if (dataInicio != null || dataFim != null) {
                // Se ao menos uma data foi fornecida, busca por período
                transacoes = transacaoDAO.buscarTransacoesPorPeriodo(idConta, dataInicio, dataFim);
            } else {
                // Se nenhuma data foi fornecida, busca todas as transações da conta
                transacoes = transacaoDAO.buscarTodasTransacoesPorConta(idConta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("erro", "Ocorreu um erro ao buscar o extrato: " + e.getMessage());
        }

        request.setAttribute("listaTransacoes", transacoes);
        request.setAttribute("dataInicio", dataInicioStr); // Repassa para o JSP preencher os campos do form
        request.setAttribute("dataFim", dataFimStr);       // Repassa para o JSP preencher os campos do form
        request.getRequestDispatcher("extrato.jsp").forward(request, response);
    }
}