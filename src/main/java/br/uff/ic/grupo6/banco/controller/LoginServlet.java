package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Usuario;
import br.uff.ic.grupo6.banco.model.Gerente;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet que controla o processo de login e autenticação dos usuários.
 */
public class LoginServlet extends HttpServlet {

    /**
     * Centraliza a lógica de processamento do login, chamada tanto por GET
     * quanto por POST.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Pega os dados enviados pelo formulário de login.jsp
        String cpf = request.getParameter("login");
        String senha = request.getParameter("senha");
        // Pega o valor da checkbox "lembrar", será "on" se marcada, ou null se desmarcada
        String lembrar = request.getParameter("lembrar");

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = null;

        // Tenta buscar o usuário no banco de dados pelo CPF fornecido
        try {
            usuario = usuarioDAO.buscarPorCpf(cpf);
        } catch (SQLException e) {
            e.printStackTrace();
            // Se der erro no banco, encaminha de volta para o login com uma mensagem de erro genérica
            request.setAttribute("erro", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return; // interrompe a execução
        }

        // verifica se o usuário foi encontrado e se a senha bate
        if (usuario != null && usuario.getSenha().equals(senha)) {
            // Login bem-sucedido

            // Cria uma nova sessão para o usuário ou pega a existente
            HttpSession sessao = request.getSession();
            // Armazena o objeto do usuário na sessão, o que efetivamente "loga" o usuário
            sessao.setAttribute("usuarioLogado", usuario);

            // funcionalidade "Lembrar meu login".
            if (lembrar != null) {
                // Se a caixa foi marcada, cria um token e um cookie persistente
                String token = UUID.randomUUID().toString(); // Gera um token único e seguro
                try {
                    // Salva o token no banco de dados, associado ao usuário
                    usuarioDAO.salvarTokenLembrarMe(usuario.getId(), token);

                    // Cria o cookie para ser armazenado no navegador
                    Cookie cookieLembrarMe = new Cookie("lembrarMeToken", token);
                    // Define o tempo de vida do cookie (em segundos). Aqui, 30 dias
                    cookieLembrarMe.setMaxAge(30 * 24 * 60 * 60);
                    // Define o caminho para que o cookie seja válido em toda a aplicação
                    cookieLembrarMe.setPath(request.getContextPath());
                    // Adiciona o cookie na resposta que vai para o navegador
                    response.addCookie(cookieLembrarMe);

                } catch (SQLException e) {
                    e.printStackTrace(); // A falha aqui não impede o login, apenas registra o erro
                }
            } else {
                // Se a caixa NÃO foi marcada, o sistema deve "esquecer" o usuário
                try {
                    // Remove o token do banco de dados para invalidá-lo
                    usuarioDAO.salvarTokenLembrarMe(usuario.getId(), null);

                    // Cria um cookie com o mesmo nome para substituí-lo
                    Cookie cookieLembrarMe = new Cookie("lembrarMeToken", "");
                    // Define o tempo de vida como 0 para instruir o navegador a apagá-lo
                    cookieLembrarMe.setMaxAge(0);
                    cookieLembrarMe.setPath(request.getContextPath());
                    response.addCookie(cookieLembrarMe);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // redireciona para o dashboard correto dependendo do tipo de usuário
            if (usuario instanceof Gerente) {
                response.sendRedirect("gerente/dashboard.jsp");
            } else {
                response.sendRedirect("dashboard.jsp");
            }
        } else {
            // Falha no login (usuário não encontrado ou senha incorreta)
            request.setAttribute("erro", "CPF ou senha invalidos.");
            // Encaminha de volta para a tela de login para exibir a mensagem de erro
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    // Métodos padrão do Servlet que apenas repassam a requisição para o processRequest
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Servlet responsável pela autenticação de usuários";
    }
}
