package br.uff.ic.grupo6.banco.controller.gerente;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class ExcluirClienteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Obtém o ID do cliente a ser excluído da requisição
            int idCliente = Integer.parseInt(request.getParameter("id"));
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.excluirClientePorId(idCliente);
            response.sendRedirect(request.getContextPath() + "/gerente/dashboard.jsp?acao=listarTodos&msg=Cliente+excluido+com+sucesso!");
        } catch (NumberFormatException e) {
            // Redireciona de volta para o dashboard com uma mensagem de erro na URL
            response.sendRedirect(request.getContextPath() + "/gerente/dashboard.jsp?erro=ID+invalido");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/gerente/dashboard.jsp?erro=Erro+ao+excluir+cliente");
        }
    }
}
