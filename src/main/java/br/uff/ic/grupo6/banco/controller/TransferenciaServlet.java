package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

public class TransferenciaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessao = request.getSession();
        Cliente clienteOrigem = (Cliente) sessao.getAttribute("usuarioLogado");

        // Se não houver cliente na sessão, redireciona para o login
        if (clienteOrigem == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            String agenciaDestino = request.getParameter("agenciaDestino");
            String contaDestino = request.getParameter("contaDestino");
            double valor = Double.parseDouble(request.getParameter("valor"));

            ContaDAO contaDAO = new ContaDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO(); // DAO para buscar dados do cliente de destino

            Conta contaOrigem = clienteOrigem.getConta();
            Conta contaDeDestino = contaDAO.buscarContaPorAgenciaENumeroDaConta(agenciaDestino, contaDestino);

            // ===== VALIDAÇÃO =====
            if (valor <= 0) {
                response.sendRedirect("transferencia.jsp?erro=Valor+deve+ser+positivo");
                return;
            }

            if (valor > contaOrigem.getSaldo()) {
                response.sendRedirect("transferencia.jsp?erro=Saldo+insuficiente");
                return;
            }

            if (contaDeDestino == null) {
                response.sendRedirect("transferencia.jsp?erro=Conta+de+destino+nao+encontrada");
                return;
            }

            // Validação para impedir transferência para a própria conta
            if (contaDeDestino.getId() == contaOrigem.getId()) {
                response.sendRedirect("transferencia.jsp?erro=Conta+de+destino+não+pode+ser+a+mesma+de+origem");
                return;
            }
            // ====== FIM DO BLOCO DE VALIDAÇÃO ======

            // 1. Busca os dados completos do cliente de destino para exibir na confirmação
            Cliente clienteDestino = usuarioDAO.buscarClientePorIdConta(contaDeDestino.getId());
            if (clienteDestino == null) {
                response.sendRedirect("transferencia.jsp?erro=Cliente+de+destino+não+encontrado");
                return;
            }

            // 2. Guarda todos os dados necessários como atributos da requisição
            request.setAttribute("clienteOrigem", clienteOrigem);
            request.setAttribute("clienteDestino", clienteDestino);
            request.setAttribute("contaDeDestino", contaDeDestino);
            request.setAttribute("valor", valor);

            // 3. Encaminha para a página de confirmação
            request.getRequestDispatcher("confirmacaoTransferencia.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect("transferencia.jsp?erro=Valor+inválido");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("dashboard.jsp?erro=Ocorreu+um+erro+ao+buscar+os+dados+da+conta");
        }
    }
}
