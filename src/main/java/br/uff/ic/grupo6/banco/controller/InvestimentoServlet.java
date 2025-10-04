package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Investimento;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class InvestimentoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Atributo para guardar a dependência
	private final ContaDAO contaDAO;

	public InvestimentoServlet() {
		this.contaDAO = new ContaDAO();
	}

	/**
	 * Construtor para testes. Permite a injeção de um mock.
	 * 
	 * @param contaDAO Uma instância (real ou mock) de ContaDAO.
	 */
	public InvestimentoServlet(ContaDAO contaDAO) {
		this.contaDAO = contaDAO;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

		if (clienteLogado == null) {
			response.sendRedirect("login.jsp");
			return;
		}

		// Garante que a codificação dos caracteres seja UTF-8 para evitar problemas com
		// acentuação
		request.setCharacterEncoding("UTF-8");

		String tipoInvestimento = request.getParameter("tipoInvestimento");
		String valorStr = request.getParameter("valor");
		double valorInvestimento;

		try {
			// Valida o tipo de investimento
			if (tipoInvestimento == null || tipoInvestimento.trim().isEmpty()
					|| !tipoInvestimento.matches("SELIC|CDB|FII|POUPANCA")) {
				String errorMessage = URLEncoder.encode("Tipo de investimento inválido.", StandardCharsets.UTF_8);
				response.sendRedirect("aplicarInvestimento.jsp?erro=" + errorMessage + "&tipoInvestimento="
						+ URLEncoder.encode(tipoInvestimento != null ? tipoInvestimento : "", StandardCharsets.UTF_8));
				return;
			}

			// Valida o valor
			valorInvestimento = Double.parseDouble(valorStr);
			if (valorInvestimento <= 0) {
				String errorMessage = URLEncoder.encode("O valor do investimento deve ser positivo.",
						StandardCharsets.UTF_8);
				response.sendRedirect("aplicarInvestimento.jsp?erro=" + errorMessage + "&tipoInvestimento="
						+ URLEncoder.encode(tipoInvestimento, StandardCharsets.UTF_8));
				return;
			}

			Conta contaCliente = clienteLogado.getConta();
			if (contaCliente == null) {
				String errorMessage = URLEncoder.encode("Conta não encontrada para o cliente logado.",
						StandardCharsets.UTF_8);
				response.sendRedirect("aplicarInvestimento.jsp?erro=" + errorMessage + "&tipoInvestimento="
						+ URLEncoder.encode(tipoInvestimento, StandardCharsets.UTF_8));
				return;
			}

			if (contaCliente.getSaldo() < valorInvestimento) {
				String errorMessage = URLEncoder.encode("Saldo insuficiente para realizar o investimento.",
						StandardCharsets.UTF_8);
				response.sendRedirect("aplicarInvestimento.jsp?erro=" + errorMessage + "&tipoInvestimento="
						+ URLEncoder.encode(tipoInvestimento, StandardCharsets.UTF_8));
				return;
			}

			Investimento investimento = this.contaDAO.realizarInvestimento(contaCliente.getId(), tipoInvestimento,
					valorInvestimento);

			// Atualiza o saldo do objeto Conta na sessão
			contaCliente.sacar(valorInvestimento);

			request.setAttribute("comprovante", investimento);
			request.getRequestDispatcher("comprovanteInvestimento.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			String errorMessage = URLEncoder.encode("Valor invalido. Por favor, insira um numero no formato correto.",
					StandardCharsets.UTF_8);
			response.sendRedirect("aplicarInvestimento.jsp?erro=" + errorMessage + "&tipoInvestimento="
					+ URLEncoder.encode(tipoInvestimento != null ? tipoInvestimento : "", StandardCharsets.UTF_8));
		} catch (SQLException e) {
			e.printStackTrace();
			String errorMessage = URLEncoder.encode(
					"Ocorreu um erro ao processar o investimento. Tente novamente mais tarde.", StandardCharsets.UTF_8);
			response.sendRedirect("aplicarInvestimento.jsp?erro=" + errorMessage + "&tipoInvestimento="
					+ URLEncoder.encode(tipoInvestimento != null ? tipoInvestimento : "", StandardCharsets.UTF_8));
		}
	}
}