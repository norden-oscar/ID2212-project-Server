package Servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Server.Lobby;

import javax.servlet.ServletException;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Lobby lobby = Lobby.getLobby();
		String result;
		String userName = request.getParameter("userName");
		String passWord = request.getParameter("passWord");
		result = lobby.registerUser(userName, passWord);
		// TODO: send register request to lobby

		response.getWriter().write(result);
	}
}