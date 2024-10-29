package servlets.chat;

import engine.chat.ChatManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "GetUserChatServlet", urlPatterns = "/sendChat")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class SendChatServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ChatManager chatManager = ServletUtils.getChatManager(getServletContext());

            String username = ServletUtils.getUserName(request);
            String userChatString = ServletUtils.getJsonBody(request);

            synchronized (getServletContext()) {
                chatManager.addChatString(userChatString, username);
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
