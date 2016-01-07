package Servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Server.Lobby;

import javax.servlet.ServletException;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Lobby lobby = Lobby.getLobby();
		String userName = request.getParameter("userName");
		String passWord = request.getParameter("passWord");

		String result;
		result = lobby.loginUser(userName, passWord);
		response.getWriter().write(result);
	}
}