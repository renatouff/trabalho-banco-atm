package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.InvestimentoDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Investimento;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MeusInvestimentosServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

        if (clienteLogado == null || !(clienteLogado instanceof Cliente)) {
            response.sendRedirect("login.jsp?erro=Acesso não autorizado. Faça login como cliente.");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String dataInicioStr = request.getParameter("dataInicio");
        String dataFimStr = request.getParameter("dataFim");
        LocalDate dataInicio = null;
        LocalDate dataFim = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
                dataInicio = LocalDate.parse(dataInicioStr, formatter);
            }
            if (dataFimStr != null && !dataFimStr.isEmpty()) {
                dataFim = LocalDate.parse(dataFimStr, formatter);
            }

            if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
                String errorMessage = URLEncoder.encode("A data de início deve ser anterior ou igual à data de fim.", StandardCharsets.UTF_8);
                response.sendRedirect("meusInvestimentos.jsp?erro=" + errorMessage);
                return;
            }

            InvestimentoDAO investimentoDAO = new InvestimentoDAO();
            List<Investimento> listaInvestimentos = investimentoDAO.buscarInvestimentosPorPeriodo(clienteLogado.getConta().getId(), dataInicio, dataFim);

            request.setAttribute("listaInvestimentos", listaInvestimentos);
            request.setAttribute("dataInicio", dataInicioStr);
            request.setAttribute("dataFim", dataFimStr);
            request.getRequestDispatcher("meusInvestimentos.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            String errorMessage = URLEncoder.encode("Ocorreu um erro ao buscar o histórico de investimentos. Tente novamente mais tarde.", StandardCharsets.UTF_8);
            response.sendRedirect("meusInvestimentos.jsp?erro=" + errorMessage);
        } catch (IllegalArgumentException e) {
            String errorMessage = URLEncoder.encode("Formato de data inválido. Use o formato AAAA-MM-DD.", StandardCharsets.UTF_8);
            response.sendRedirect("meusInvestimentos.jsp?erro=" + errorMessage);
        }
    }
}