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

public class DepositoServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessao = request.getSession();
        Cliente cliente = (Cliente) sessao.getAttribute("usuarioLogado");

        if (cliente == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            double valor = Double.parseDouble(request.getParameter("valor"));
            if (valor <= 0) {
                // Mensagem de erro para a URL
                String mensagemErro = "O valor do depósito deve ser positivo.";
                response.sendRedirect("deposito.jsp?erro=" + URLEncoder.encode(mensagemErro, StandardCharsets.UTF_8));
                return;
            }

            ContaDAO contaDAO = new ContaDAO();
            Conta contaDoCliente = cliente.getConta();

            Transacao transacao = contaDAO.realizarDeposito(contaDoCliente.getId(), valor);
            contaDoCliente.depositar(valor);
            request.setAttribute("comprovante", transacao);
            request.getRequestDispatcher("comprovanteDeposito.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Mensagem de erro para a URL
            String mensagemErro = "Valor inválido. Use apenas números e ponto ou vírgula.";
            response.sendRedirect("deposito.jsp?erro=" + URLEncoder.encode(mensagemErro, StandardCharsets.UTF_8));
        } catch (SQLException e) {
            e.printStackTrace();
            String mensagemErro = "Ocorreu um erro ao processar o depósito.";
            response.sendRedirect("dashboard.jsp?erro=" + URLEncoder.encode(mensagemErro, StandardCharsets.UTF_8));
        }
    }
}
