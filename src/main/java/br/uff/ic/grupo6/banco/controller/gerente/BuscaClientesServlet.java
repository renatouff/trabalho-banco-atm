package br.uff.ic.grupo6.banco.controller.gerente;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BuscaClientesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String termoBusca = request.getParameter("termoBusca");
        String acao = request.getParameter("acao");

        List<Cliente> clientesEncontrados = new ArrayList<>();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        try {
            // Se a ação for 'listarTodos', chama o novo método do DAO
            if ("listarTodos".equals(acao)) {
                clientesEncontrados = usuarioDAO.buscarTodosClientesOrdenados();
            } // Senão, se houver um termo de busca, faz a busca normal
            else if (termoBusca != null && !termoBusca.trim().isEmpty()) {
                clientesEncontrados = usuarioDAO.buscarClientesPorTermo(termoBusca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("listaClientes", clientesEncontrados);
        request.getRequestDispatcher("/gerente/tabelaClientes.jsp").include(request, response);
    }
}
