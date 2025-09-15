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
import java.net.URLEncoder; // Importar para codificar mensagens de erro na URL
import java.nio.charset.StandardCharsets; // Importar para especificar o charset

public class SaqueServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

        if (clienteLogado == null || !(clienteLogado instanceof Cliente)) {
            // Usar URLEncoder para codificar a mensagem de erro na URL
            String errorMessage = URLEncoder.encode("Acesso não autorizado. Faça login como cliente.", StandardCharsets.UTF_8);
            response.sendRedirect("login.jsp?erro=" + errorMessage);
            return;
        }

        // Garante que a codificação dos caracteres seja UTF-8 para evitar problemas com acentuação
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
            Transacao transacaoSaque = contaDAO.realizarSaque(contaCliente.getId(), valorSaque);

            // Atualiza o saldo do objeto Conta na sessão do cliente
            contaCliente.sacar(valorSaque); // Utiliza o método sacar da classe Conta para atualizar o saldo do objeto
            // Não precisa re-setar o cliente na sessão se você já pegou a referência direta ao objeto 'conta' e 'clienteLogado'

            request.setAttribute("comprovante", transacaoSaque);
            // request.setAttribute("cliente", clienteLogado); // O cliente já está na sessão, não precisa passar novamente

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