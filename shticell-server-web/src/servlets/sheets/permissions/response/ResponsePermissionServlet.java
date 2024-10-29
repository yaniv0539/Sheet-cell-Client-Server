package servlets.sheets.permissions.response;

import com.google.gson.Gson;
import dto.RequestDto;
import dto.enums.PermissionType;
import dto.enums.Status;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "ResponsePermissionServlet", urlPatterns = "/permissions/response")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class ResponsePermissionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String userName = ServletUtils.getUserName(request);
            String sheetName = ServletUtils.getSheetName(request);
            PermissionType permissionType = ServletUtils.getPermissionType(request);
            Status status = ServletUtils.getStatus(request);
            Status responseToRequest = ServletUtils.getResponse(request);

            engine.setResponseToRequest(sheetName,userName,permissionType,status,responseToRequest);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().print("request has update !");
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
