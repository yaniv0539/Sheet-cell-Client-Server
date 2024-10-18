package servlets.ranges;

import com.google.gson.Gson;
import utils.Constants;
import dto.RangeDto;
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

            String rangeValue = request.getParameter(Constants.RANGE_BOUNDARIES_PARAMETER);

            if (rangeValue == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Range value is required");
            }

            engine.addRange(sheetName, rangeName, rangeValue);

            SheetDto sheetDTO = engine.getSheetDTO(sheetName);

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

            String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

            if (sheetName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Sheet name is required");
            }

            String rangeName = request.getParameter("rangeName");

            if (rangeName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Range name is required");
            }

            engine.deleteRange(sheetName, rangeName);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
