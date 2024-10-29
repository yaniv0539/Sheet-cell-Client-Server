package servlets.chat;

import com.google.gson.Gson;
import engine.chat.ChatManager;
import engine.chat.SingleChatEntry;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "ChatServlet", urlPatterns = "/chat")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class ChatServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ChatManager chatManager = ServletUtils.getChatManager(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            int chatVersion = ServletUtils.getChatVersion(request);

            int chatManagerVersion;

            List<SingleChatEntry> chatEntries;

            synchronized (getServletContext()) {
                chatManagerVersion = chatManager.getVersion();
                chatEntries = chatManager.getChatEntries(chatVersion);
            }

            ChatAndVersion cav = new ChatAndVersion(chatEntries, chatManagerVersion);

            PrintWriter out = response.getWriter();
            out.print(gson.toJson(cav));
            out.flush();

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private static class ChatAndVersion {

        final private List<SingleChatEntry> entries;
        final private int version;

        public ChatAndVersion(List<SingleChatEntry> entries, int version) {
            this.entries = entries;
            this.version = version;
        }
    }
}