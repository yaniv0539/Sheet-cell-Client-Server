package servlets.sheets.permissions;

import com.google.gson.Gson;
import dto.PermissionsDto;
import dto.RequestDto;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "PermissionsServlet", urlPatterns = "/permissions")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class PermissionsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String sheetName = ServletUtils.getSheetName(request);

            PermissionsDto permissionsDto = engine.getSheetPermissions(sheetName);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().print(gson.toJson(permissionsDto));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
