package servlets.overview;

import com.google.gson.Gson;
import dto.SheetOverviewDto;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebServlet(name = "OverviewServlet", urlPatterns = "/Overview")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class OverviewServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String userName = ServletUtils.getUserName(request);

            Set<SheetOverviewDto> sheetOverviewDtoSet = engine.getSheetOverviewDto(userName);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(sheetOverviewDtoSet));
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }




    }
}
