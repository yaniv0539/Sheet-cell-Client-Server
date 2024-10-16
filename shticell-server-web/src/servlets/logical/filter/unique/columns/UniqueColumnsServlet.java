package servlets.logical.filter.unique.columns;

import com.google.gson.Gson;
import constants.Constants;
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

            String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

            if (sheetName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Sheet name is required");
            }

            String sheetVersion = request.getParameter(Constants.SHEET_VERSION_PARAMETER);

            if (sheetVersion == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Range name is required");
            }

            String column = request.getParameter(Constants.SHEET_COLUMN_PARAMETER);

            if (column == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Column name is required");
            }

            String startRow = request.getParameter(Constants.SHEET_START_ROW_PARAMETER);

            if (startRow == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Start row is required");
            }

            String endRow = request.getParameter(Constants.SHEET_END_ROW_PARAMETER);

            if (endRow == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("End row is required");
            }

            List<String> columnUniqueValuesInRange = engine.getColumnUniqueValuesInRange(
                    sheetName,
                    Integer.parseInt(column),
                    Integer.parseInt(startRow),
                    Integer.parseInt(endRow),
                    Integer.parseInt(sheetVersion));

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
