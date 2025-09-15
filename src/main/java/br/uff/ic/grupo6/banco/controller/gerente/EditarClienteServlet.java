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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class EditarClienteServlet extends HttpServlet {

    // doGet: Carrega os dados e mostra o formulário de edição
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Segurança: Apenas gerentes podem acessar
        if (!(request.getSession().getAttribute("usuarioLogado") instanceof Gerente)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?erro=Acesso Negado");
            return;
        }

        try {
            int idCliente = Integer.parseInt(request.getParameter("id"));
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Cliente cliente = usuarioDAO.buscarClientePorId(idCliente);

            if (cliente != null) {
                request.setAttribute("clienteParaEditar", cliente);
                request.getRequestDispatcher("/gerente/editarCliente.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/gerente/dashboard.jsp?erro=Cliente+nao+encontrado");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/gerente/dashboard.jsp?erro=ID+invalido");
        } catch (SQLException e) {
            throw new ServletException("Erro de banco de dados ao buscar cliente", e);
        }
    }

    // doPost: Recebe os dados do formulário e salva no banco
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Segurança: Apenas gerentes podem acessar
        if (!(request.getSession().getAttribute("usuarioLogado") instanceof Gerente)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?erro=Acesso+Negado");
            return;
        }

        // 1. Coleta todos os dados do formulário
        Cliente clienteAtualizado = new Cliente();
        try {
            clienteAtualizado.setId(Integer.parseInt(request.getParameter("id")));
            clienteAtualizado.setNome(request.getParameter("nome"));
            clienteAtualizado.setEmail(request.getParameter("email"));
            clienteAtualizado.setTelefone(request.getParameter("telefone"));
            clienteAtualizado.setCep(request.getParameter("cep"));
            clienteAtualizado.setEndereco(request.getParameter("endereco"));
            clienteAtualizado.setBairro(request.getParameter("bairro"));
            clienteAtualizado.setCidade(request.getParameter("cidade"));
            clienteAtualizado.setEstado(request.getParameter("estado"));
            clienteAtualizado.setOcupacao(request.getParameter("ocupacao"));

            // Tratamento especial para data e números
            clienteAtualizado.setDataNascimento(LocalDate.parse(request.getParameter("dataNascimento")));
            clienteAtualizado.setRenda(Double.parseDouble(request.getParameter("renda")));

        } catch (NumberFormatException | DateTimeParseException e) {
            // Se houver erro de formato (ID, Renda, Data), volta para a página de edição com erro
            request.setAttribute("erro", "Formato de dados inválido. Verifique os campos de data e renda.");
            // Preenche novamente o formulário com os dados digitados
            request.setAttribute("clienteParaEditar", clienteAtualizado);
            request.getRequestDispatcher("/gerente/editarCliente.jsp").forward(request, response);
            return;
        }

        // 2. Atualiza os dados no banco
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        try {
            usuarioDAO.atualizarCliente(clienteAtualizado);
            // 3. Redireciona para o dashboard com mensagem de sucesso
            response.sendRedirect(request.getContextPath() + "/gerente/dashboard.jsp?msg=Cliente+atualizado+com+sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao salvar no banco de dados: " + e.getMessage());
            request.setAttribute("clienteParaEditar", clienteAtualizado);
            request.getRequestDispatcher("/gerente/editarCliente.jsp").forward(request, response);
        }
    }
}
