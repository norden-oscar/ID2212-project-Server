package Servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Server.Lobby;

import javax.servlet.ServletException;
import java.io.IOException;

public class FetchProfileServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String userName = request.getParameter("userName");
		String remoteAddr = request.getRemoteAddr();

		Lobby lobby =Lobby.getLobby();
		String[] result = lobby.fetchProfile(userName);
		String resultToString = result[0]+" "+result[1]+" "+result[2];
		response.getWriter().write(resultToString);
	}
}