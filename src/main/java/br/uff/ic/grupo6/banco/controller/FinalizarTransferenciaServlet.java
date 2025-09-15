package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
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
import java.util.List;

/**
 * Recebe a confirmação final do usuário e executa a operação no banco de dados,
 * gerando um comprovante ao final.
 */
public class FinalizarTransferenciaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessao = request.getSession();
        Cliente cliente = (Cliente) sessao.getAttribute("usuarioLogado");

        if (cliente == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            // 1. Coleta os dados enviados pela página de confirmação
            int idContaOrigem = Integer.parseInt(request.getParameter("idContaOrigem"));
            int idContaDestino = Integer.parseInt(request.getParameter("idContaDestino"));
            double valor = Double.parseDouble(request.getParameter("valor"));

            // Para exibir no comprovante final, coletamos os dados do destinatário,
            // que devem ser passados como campos hidden do formulário de confirmação
            String nomeDestino = request.getParameter("nomeDestino");
            String cpfDestino = request.getParameter("cpfDestino");
            String agenciaDestino = request.getParameter("agenciaDestino");
            String numeroContaDestino = request.getParameter("numeroContaDestino");

            // 2. Executa a transferência no banco de dados
            ContaDAO contaDAO = new ContaDAO();
            // O método retorna uma lista com as duas transações (débito e crédito)
            List<Transacao> transacoes = contaDAO.realizarTransferencia(idContaOrigem, idContaDestino, valor);

            // 3. Atualiza o saldo do cliente logado na sessão
            cliente.getConta().sacar(valor);

            // 4. Prepara os dados para o comprovante final
            Transacao comprovanteRemetente = null;
            for (Transacao t : transacoes) {
                if (t.getIdConta() == idContaOrigem) {
                    comprovanteRemetente = t;
                    break;
                }
            }

            // Adiciona todos os dados necessários na requisição para a página de comprovante
            request.setAttribute("comprovante", comprovanteRemetente);
            request.setAttribute("nomeDestino", nomeDestino);
            request.setAttribute("cpfDestino", cpfDestino);
            request.setAttribute("agenciaDestino", agenciaDestino);
            request.setAttribute("numeroContaDestino", numeroContaDestino);

            // 5. Encaminha para a página de comprovante
            request.getRequestDispatcher("comprovanteTransferencia.jsp").forward(request, response);

        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            response.sendRedirect("dashboard.jsp?erro=Ocorreu+um+erro+ao+finalizar+a+transferencia");
        }
    }
}
