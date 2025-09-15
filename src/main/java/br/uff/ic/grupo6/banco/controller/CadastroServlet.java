package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CadastroServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Garante que os dados do formulário sejam lidos como UTF-8
        request.setCharacterEncoding("UTF-8");

        // Obter todos os parâmetros do formulário
        String nome = request.getParameter("nome");
        String cpf = request.getParameter("cpf");
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
        String senha = request.getParameter("senha");
        String confirmaSenha = request.getParameter("confirmaSenha");

        // --- VALIDAÇÕES ---
        if (!senha.equals(confirmaSenha)) {
            request.setAttribute("mensagem", "Erro: As senhas não conferem!");
            request.getRequestDispatcher("cadastro.jsp").forward(request, response);
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        try {
            if (usuarioDAO.buscarPorCpf(cpf) != null) {
                request.setAttribute("mensagem", "Erro: Este CPF já está cadastrado!");
                request.getRequestDispatcher("cadastro.jsp").forward(request, response);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("mensagem", "Erro ao verificar o CPF. Tente novamente.");
            request.getRequestDispatcher("cadastro.jsp").forward(request, response);
            return;
        }

        // --- CRIAÇÃO DO OBJETO CLIENTE ---
        Cliente novoCliente = new Cliente(cpf, senha, nome, cpf);

        try {
            novoCliente.setDataNascimento(LocalDate.parse(dataNascimentoStr));
            novoCliente.setEmail(email);
            novoCliente.setTelefone(telefone);
            novoCliente.setCep(cep);
            novoCliente.setEndereco(endereco);
            novoCliente.setBairro(bairro);
            novoCliente.setCidade(cidade);
            novoCliente.setEstado(estado);
            novoCliente.setRenda(Double.parseDouble(rendaStr));
            novoCliente.setOcupacao(ocupacao);
        } catch (DateTimeParseException | NumberFormatException e) {
            request.setAttribute("mensagem", "Erro: Formato de data ou renda inválido.");
            request.getRequestDispatcher("cadastro.jsp").forward(request, response);
            return;
        }

        // --- PERSISTÊNCIA NO BANCO DE DADOS ---
        try {
            usuarioDAO.cadastrarCliente(novoCliente);
            String mensagemSucesso = "Cadastro realizado com sucesso! Faça seu login.";
            String mensagemCodificada = URLEncoder.encode(mensagemSucesso, StandardCharsets.UTF_8.toString());
            response.sendRedirect("login.jsp?msg=" + mensagemCodificada);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("mensagem", "Erro ao salvar os dados no banco. Tente novamente.");
            request.getRequestDispatcher("cadastro.jsp").forward(request, response);
        }
    }
}
