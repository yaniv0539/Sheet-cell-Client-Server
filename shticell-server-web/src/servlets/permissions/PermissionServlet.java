package servlets.permissions;

import com.google.gson.Gson;
import dto.SheetDto;
import dto.enums.PermissionType;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

public class PermissionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String userName = ServletUtils.getSheetName(request);
            String sheetName = ServletUtils.getSheetName(request);
            PermissionType permissionType = ServletUtils.getPermissionType(request);

            // TODO: Call the right method from the engine.

            SheetDto sheetDTO = engine.getSheetDTO(userName, sheetName);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().print(gson.toJson(sheetDTO));
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
