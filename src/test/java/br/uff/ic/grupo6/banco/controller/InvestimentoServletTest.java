package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Investimento;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestimentoServletTest {

	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private HttpSession session;
	@Mock
	private RequestDispatcher requestDispatcher;

	// Mock do DAO que será injetado
	@Mock
	private ContaDAO contaDAO;

	// A instância do Servlet que vamos testar
	private InvestimentoServlet servlet;

	@BeforeEach
	void setUp() {
		// injetamos o mock do DAO no construtor do Servlet
		servlet = new InvestimentoServlet(contaDAO);

		when(request.getSession()).thenReturn(session);
	}

	@Test
	@DisplayName("Deve redirecionar para login se nao houver cliente logado")
	void doPost_QuandoClienteNaoLogado_DeveRedirecionarParaLogin() throws ServletException, IOException {
		when(session.getAttribute("usuarioLogado")).thenReturn(null);
		servlet.doPost(request, response);
		verify(response).sendRedirect("login.jsp");
	}

	@Test
	@DisplayName("Deve redirecionar com erro se o tipo de investimento for invalido")
	void doPost_QuandoTipoInvestimentoInvalido_DeveRedirecionarComErro() throws ServletException, IOException {
		when(session.getAttribute("usuarioLogado")).thenReturn(new Cliente());
		when(request.getParameter("tipoInvestimento")).thenReturn("TIPO_INVALIDO");
		servlet.doPost(request, response);
		
		verify(response).sendRedirect(contains("Tipo+de+investimento+inv%C3%A1lido."));
	}

	@Test
	@DisplayName("Deve redirecionar com erro se o valor do investimento for negativo")
	void doPost_QuandoValorInvestimentoNegativo_DeveRedirecionarComErro() throws ServletException, IOException {
		when(session.getAttribute("usuarioLogado")).thenReturn(new Cliente());
		when(request.getParameter("tipoInvestimento")).thenReturn("SELIC");
		when(request.getParameter("valor")).thenReturn("-100.00");
		servlet.doPost(request, response);
		verify(response).sendRedirect(contains("O+valor+do+investimento+deve+ser+positivo."));
	}

	@Test
	@DisplayName("Deve redirecionar com erro se o saldo for insuficiente")
	void doPost_QuandoSaldoInsuficiente_DeveRedirecionarComErro() throws ServletException, IOException {
		Cliente cliente = new Cliente();
		Conta contaComPoucoSaldo = new Conta("0001", "123", 50.0); // Saldo de R$ 50
		cliente.setConta(contaComPoucoSaldo);
		when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
		when(request.getParameter("tipoInvestimento")).thenReturn("CDB");
		when(request.getParameter("valor")).thenReturn("100.00"); // Tentando investir R$ 100
		servlet.doPost(request, response);
		verify(response).sendRedirect(contains("Saldo+insuficiente+para+realizar+o+investimento."));
	}

	@Test
	@DisplayName("Deve encaminhar para a página de comprovante em caso de sucesso")
	void doPost_QuandoInvestimentoValido_DeveEncaminharParaComprovante() throws Exception {

		Cliente cliente = new Cliente();
		Conta contaComSaldo = new Conta("0001", "123", 1000.0);
		contaComSaldo.setId(1);
		cliente.setConta(contaComSaldo);

		// Simulação dos dados vindos da requisição
		when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
		when(request.getParameter("tipoInvestimento")).thenReturn("FII");
		when(request.getParameter("valor")).thenReturn("500.00");

		// Objeto de retorno para o DAO mockado
		Investimento investimentoComprovante = new Investimento();
		investimentoComprovante.setValorAplicado(new BigDecimal("500.00"));
		investimentoComprovante.setTipoInvestimento("FII");

		// mock do DAO: quando o método 'realizarInvestimento' for chamado
		// com QUALQUER int, string e double, ele deve retornar o objeto.
		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble())).thenReturn(investimentoComprovante);

		// mock do request para retornar dispatcher mockado
		when(request.getRequestDispatcher("comprovanteInvestimento.jsp")).thenReturn(requestDispatcher);

		servlet.doPost(request, response);

		// O método do DAO foi chamado com os valores corretos?
		verify(contaDAO).realizarInvestimento(1, "FII", 500.00);

		// O saldo do objeto na sessão foi atualizado?
		assertEquals(500.0, contaComSaldo.getSaldo(), "O saldo na sessão deveria ser atualizado para 500.0");

		// O comprovante foi colocado como atributo na requisição?
		verify(request).setAttribute("comprovante", investimentoComprovante);

		// O servlet encaminhou para a página de comprovante?
		verify(requestDispatcher).forward(request, response);
	}

	@Test
	@DisplayName("Deve redirecionar com erro se o DAO lancar uma SQLException")
	void doPost_QuandoDaoLancaSQLException_DeveRedirecionarComErro() throws Exception {

		Cliente cliente = new Cliente();
		Conta contaComSaldo = new Conta("0001", "123", 1000.0);
		contaComSaldo.setId(1);
		cliente.setConta(contaComSaldo);

		when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
		when(request.getParameter("tipoInvestimento")).thenReturn("CDB");
		when(request.getParameter("valor")).thenReturn("200.00");

		// mock para lançar exceção quando for chamado
		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble()))
				.thenThrow(new SQLException("Erro simulado de conexao com o banco de dados"));

		servlet.doPost(request, response);

		// Verificamos se o servlet capturou a exceção e redirecionou para a página de
		// erro genérico
		verify(response).sendRedirect(contains("Ocorreu+um+erro+ao+processar+o+investimento."));
	}
}