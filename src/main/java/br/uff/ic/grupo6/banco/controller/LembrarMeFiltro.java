package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Filtro responsável pela funcionalidade "Lembrar meu login". Ele intercepta as
 * requisições para verificar se existe um cookie de login e, se houver,
 * pré-preenche o CPF na tela de login.
 */
public class LembrarMeFiltro implements Filter {

    /**
     * Método principal do filtro, executado para cada requisição que passa por
     * ele.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Converte o request genérico para um request HTTP, para termos acesso a sessões e cookies
        HttpServletRequest req = (HttpServletRequest) request;
        // Pega a sessão atual, mas sem criar uma nova (parâmetro false).
        HttpSession session = req.getSession(false);

        // só é executada se o usuário AINDA NÃO estiver logado
        if (session == null || session.getAttribute("usuarioLogado") == null) {

            // Pega todos os cookies que o navegador enviou na requisição
            Cookie[] cookies = req.getCookies();

            if (cookies != null) {
                // Percorre a lista de cookies...
                for (Cookie cookie : cookies) {
                    // ...procurando pelo nosso cookie específico, chamado "lembrarMeToken".
                    if ("lembrarMeToken".equals(cookie.getName())) {

                        // Se achou, pega o valor dele (que é o token de segurança)
                        String token = cookie.getValue();

                        if (token != null && !token.isEmpty()) {
                            UsuarioDAO usuarioDAO = new UsuarioDAO();
                            try {
                                // Busca no banco de dados se algum usuário possui aquele token
                                Usuario usuario = usuarioDAO.buscarUsuarioPorToken(token);
                                if (usuario != null) {
                                    // Se encontrou, adiciona o CPF do usuário como um atributo na requisição
                                    // A página login.jsp vai ler este atributo para pré-preencher o campo
                                    request.setAttribute("cpfLembrado", usuario.getLogin());
                                }
                            } catch (SQLException e) {
                                // Se der erro no banco, apenas imprime no console, não quebra a aplicação
                                e.printStackTrace();
                            }
                        }
                        // interrompe o loop, pois já achamos o cookie que queríamos.
                        break;
                    }
                }
            }
        }

        // passa a requisição adiante para o próximo filtro ou para o servlet de destino
        // Sem isso, a requisição "morreria" aqui e a página não carregaria
        chain.doFilter(request, response);
    }

    /**
     * Método de inicialização do filtro (não utilizado neste caso).
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Método de destruição do filtro (não utilizado neste caso).
     */
    @Override
    public void destroy() {
    }
}
