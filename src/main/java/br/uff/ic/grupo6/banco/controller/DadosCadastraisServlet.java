package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class DadosCadastraisServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession(false); // Não cria uma nova sessão se não existir

        if (sessao == null || !(sessao.getAttribute("usuarioLogado") instanceof Cliente)) {
            // Se não há sessão ou o usuário não é um cliente, redireciona para o login
            response.sendRedirect("login.jsp");
            return;
        }

        // Pega o cliente da sessão atual
        Cliente clienteDaSessao = (Cliente) sessao.getAttribute("usuarioLogado");

        // Busca os dados mais recentes do cliente no banco de dados
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        try {
            Cliente clienteAtualizado = (Cliente) usuarioDAO.buscarPorCpf(clienteDaSessao.getCpf());

            if (clienteAtualizado != null) {
                // Coloca o objeto cliente ATUALIZADO como um atributo na requisição
                request.setAttribute("cliente", clienteAtualizado);
                // Encaminha para a página JSP exibir os dados
                request.getRequestDispatcher("dadosCadastrais.jsp").forward(request, response);
            } else {
                // Caso raro em que o usuário da sessão não é mais encontrado no banco
                response.sendRedirect("login.jsp?msg=Usuário não encontrado.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Em caso de erro de banco, pode redirecionar para uma página de erro
            response.sendRedirect("dashboard.jsp");
        }
    }
}
