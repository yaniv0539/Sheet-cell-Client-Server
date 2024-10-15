package servlets.logical;

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

@WebServlet(name = "SortServlet", urlPatterns = "/sheet/sort")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class SortServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Engine engine = ServletUtils.getEngine(getServletContext());
        Gson gson = ServletUtils.getGson(getServletContext());

        String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

        if (sheetName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new RuntimeException("Sheet name is required");
        }

        String rangeName = request.getParameter(Constants.RANGE_NAME_PARAMETER);

        if (rangeName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new RuntimeException("Range name is required");
        }
    }
}
