package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet responsável por receber os dados do formulário de "Meus Dados
 * Cadastrais" e atualizar as informações do cliente no banco de dados.
 */
public class AtualizarDadosServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Pega a sessão atual, sem criar uma nova se não existir
        HttpSession sessao = request.getSession(false);

        // se não há sessão ou o usuário não é um cliente, volta para o login.
        if (sessao == null || !(sessao.getAttribute("usuarioLogado") instanceof Cliente)) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Pega os dados do cliente que já está logado na sessão
        Cliente clienteDaSessao = (Cliente) sessao.getAttribute("usuarioLogado");
        int idCliente = clienteDaSessao.getId();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        // 1. Coleta os dados do formulário que foram enviados via POST
        String nome = request.getParameter("nome");
        String dataNascimentoStr = request.getParameter("dataNascimento");
        String email = request.getParameter("email");
        String telefone = request.getParameter("telefone");
        String cep = request.getParameter("cep");
        String endereco = request.getParameter("endereco");
        String bairro = request.getParameter("bairro");
        String cidade = request.getParameter("cidade");
        String estado = request.getParameter("estado");
        String rendaStr = request.getParameter("renda");
        String ocupacao = request.getParameter("ocupacao");

        // Coleta os campos de senha
        String senhaAtual = request.getParameter("senhaAtual");
        String novaSenha = request.getParameter("novaSenha");
        String confirmarNovaSenha = request.getParameter("confirmarNovaSenha");

        // atualização de senha
        // Verifica se o usuário realmente preencheu o campo de nova senha
        if (novaSenha != null && !novaSenha.isEmpty()) {

            // Valida se a senha atual digitada corresponde à senha do usuário na sessão
            if (!senhaAtual.equals(clienteDaSessao.getSenha())) {
                String msg = "A senha atual esta incorreta.";
                response.sendRedirect("DadosCadastraisServlet?erro=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return; // Interrompe a execução se a senha atual estiver errada
            }
            // Valida se a nova senha e a confirmação são idênticas
            if (!novaSenha.equals(confirmarNovaSenha)) {
                String msg = "A nova senha e a confirmacao nao correspondem.";
                response.sendRedirect("DadosCadastraisServlet?erro=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return; // Interrompe a execução se não corresponderem
            }

            // Se as validações passaram, chama o DAO para atualizar a senha no banco
            try {
                usuarioDAO.atualizarSenha(idCliente, novaSenha);
                // Atualiza também o objeto na sessão para manter tudo consistente
                clienteDaSessao.setSenha(novaSenha);
            } catch (SQLException e) {
                e.printStackTrace();
                String msg = "Ocorreu um erro ao tentar atualizar a senha.";
                response.sendRedirect("DadosCadastraisServlet?erro=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return;
            }
        }

        // 2. Cria um objeto Cliente com os novos dados
        Cliente clienteParaAtualizar = new Cliente(clienteDaSessao.getLogin(), clienteDaSessao.getSenha(), nome, clienteDaSessao.getCpf());
        clienteParaAtualizar.setId(idCliente);
        clienteParaAtualizar.setDataNascimento(LocalDate.parse(dataNascimentoStr));
        clienteParaAtualizar.setEmail(email);
        clienteParaAtualizar.setTelefone(telefone);
        clienteParaAtualizar.setCep(cep);
        clienteParaAtualizar.setEndereco(endereco);
        clienteParaAtualizar.setBairro(bairro);
        clienteParaAtualizar.setCidade(cidade);
        clienteParaAtualizar.setEstado(estado);
        clienteParaAtualizar.setRenda(Double.parseDouble(rendaStr));
        clienteParaAtualizar.setOcupacao(ocupacao);

        // Mantém a referência ao objeto Conta do cliente que já estava na sessão
        Conta contaDoCliente = clienteDaSessao.getConta();
        clienteParaAtualizar.setConta(contaDoCliente);

        // 3. Chama o DAO para persistir as alterações dos outros dados
        try {
            usuarioDAO.atualizarCliente(clienteParaAtualizar);

            // Sucesso: Atualiza o objeto COMPLETO na sessão com os novos dados
            sessao.setAttribute("usuarioLogado", clienteParaAtualizar);

            // Redireciona de volta para a página de perfil com mensagem de sucesso
            String msg = "Dados atualizados com sucesso!";
            // Se a senha foi alterada, adiciona um aviso extra na mensagem de sucesso
            if (novaSenha != null && !novaSenha.isEmpty()) {
                msg += " Sua senha tambem foi alterada.";
            }
            response.sendRedirect("PerfilServlet?msg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));

        } catch (SQLException e) {
            e.printStackTrace();
            // Falha: Redireciona de volta com uma mensagem de erro
            String msg = "Nao foi possivel atualizar os dados. Tente novamente.";
            response.sendRedirect("DadosCadastraisServlet?erro=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
        }
    }
}
