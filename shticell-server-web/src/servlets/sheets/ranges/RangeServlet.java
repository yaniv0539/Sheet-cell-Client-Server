package servlets.sheets.ranges;

import com.google.gson.Gson;
import dto.SheetDto;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "RangeServlet", urlPatterns = "/sheet/ranges")
public class RangeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String userName = ServletUtils.getUserName(request);
            String sheetName = ServletUtils.getSheetName(request);
            String rangeName = ServletUtils.getRangeName(request);
            String rangeValue = ServletUtils.getBoundaries(request);

            int sheetVersion = ServletUtils.getSheetVersion(request);

            engine.addRange(userName, sheetName, sheetVersion, rangeName, rangeValue);

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

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());

            String userName = ServletUtils.getUserName(request);
            String sheetName = ServletUtils.getSheetName(request);
            String rangeName = ServletUtils.getRangeName(request);

            int sheetVersion = ServletUtils.getSheetVersion(request);

            engine.deleteRange(userName, sheetName, sheetVersion, rangeName);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
