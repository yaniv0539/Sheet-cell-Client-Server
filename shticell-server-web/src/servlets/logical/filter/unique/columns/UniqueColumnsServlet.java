package servlets.logical.filter.unique.columns;

import com.google.gson.Gson;
import utils.Constants;
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

@WebServlet(name = "UniqueColumnsServlet", urlPatterns = "/sheet/filter/uniqueColumnValues")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class UniqueColumnsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String userName = ServletUtils.getUserName(request);
            String sheetName = ServletUtils.getSheetName(request);
            int sheetVersion = ServletUtils.getSheetVersion(request);
            int column = ServletUtils.getColumn(request);
            int startRow = ServletUtils.getStartRow(request);
            int endRow = ServletUtils.getEndRow(request);

            List<String> columnUniqueValuesInRange = engine.getColumnUniqueValuesInRange(userName, sheetName, sheetVersion, column, startRow, endRow);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(columnUniqueValuesInRange));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
