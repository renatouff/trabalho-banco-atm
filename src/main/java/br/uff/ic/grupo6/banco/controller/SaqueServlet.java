package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SaqueServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        
        Object usuarioLogadoObj = session.getAttribute("usuarioLogado");

        if (usuarioLogadoObj == null || !(usuarioLogadoObj instanceof Cliente)) {
            String errorMessage = URLEncoder.encode("Acesso não autorizado. Faça login como cliente.", StandardCharsets.UTF_8);
            response.sendRedirect("login.jsp?erro=" + errorMessage);
            return;
        }

        Cliente clienteLogado = (Cliente) usuarioLogadoObj;
        request.setCharacterEncoding("UTF-8");

        String valorStr = request.getParameter("valor");
        double valorSaque;

        try {
            valorSaque = Double.parseDouble(valorStr);

            if (valorSaque <= 0) {
                String errorMessage = URLEncoder.encode("O valor do saque deve ser positivo.", StandardCharsets.UTF_8);
                response.sendRedirect("saque.jsp?erro=" + errorMessage);
                return;
            }

            
            if (valorSaque > 2000) {
                String errorMessage = URLEncoder.encode("O valor do saque excede o limite de R$ 2.000,00 por transação.", StandardCharsets.UTF_8);
                response.sendRedirect("saque.jsp?erro=" + errorMessage);
                return;
            }

           
            if (valorSaque % 10 != 0) {
                String errorMessage = URLEncoder.encode("O valor do saque deve ser em múltiplos de R$ 10,00.", StandardCharsets.UTF_8);
                response.sendRedirect("saque.jsp?erro=" + errorMessage);
                return;
            }

            Conta contaCliente = clienteLogado.getConta();
            if (contaCliente == null) {
                String errorMessage = URLEncoder.encode("Conta não encontrada para o cliente logado.", StandardCharsets.UTF_8);
                response.sendRedirect("saque.jsp?erro=" + errorMessage);
                return;
            }
            
            if (contaCliente.getSaldo() < valorSaque) {
                String errorMessage = URLEncoder.encode("Saldo insuficiente para realizar o saque.", StandardCharsets.UTF_8);
                response.sendRedirect("saque.jsp?erro=" + errorMessage);
                return;
            }

            ContaDAO contaDAO = new ContaDAO();
            // Assumindo que o ID da conta é Long, mas o DAO espera int. O cast é necessário.
            Transacao transacaoSaque = contaDAO.realizarSaque((int) contaCliente.getId(), valorSaque);

            contaCliente.sacar(valorSaque); 

            request.setAttribute("comprovante", transacaoSaque);
            request.getRequestDispatcher("comprovanteSaque.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            String errorMessage = URLEncoder.encode("Valor inválido. Por favor, insira um número no formato correto.", StandardCharsets.UTF_8);
            response.sendRedirect("saque.jsp?erro=" + errorMessage);
        } catch (SQLException e) {
            e.printStackTrace();
            String errorMessage = URLEncoder.encode("Ocorreu um erro ao processar o saque. Tente novamente mais tarde.", StandardCharsets.UTF_8);
            response.sendRedirect("saque.jsp?erro=" + errorMessage);
        }
    }
}