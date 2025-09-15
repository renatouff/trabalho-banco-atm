package br.uff.ic.grupo6.banco.controller.gerente;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Gerente;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListaClientesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Verifica se o usuário é um gerente
        if (!(request.getSession().getAttribute("usuarioLogado") instanceof Gerente)) {
            response.sendRedirect("../login.jsp?erro=Acesso Negado");
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        try {
            List<Cliente> listaClientes = usuarioDAO.buscarTodosClientes();
            request.setAttribute("listaClientes", listaClientes);
            // Encaminha para um JSP que irá renderizar a tabela
            request.getRequestDispatcher("/gerente/tabelaClientes.jsp").include(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao carregar a lista de clientes.");
            request.getRequestDispatcher("/gerente/tabelaClientes.jsp").include(request, response);
        }
    }
}
