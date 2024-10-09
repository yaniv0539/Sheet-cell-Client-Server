package servlets;

import com.google.gson.Gson;
import constants.Constants;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "CellServlet", urlPatterns = {"/sheet/cell"})
public class CellServlet extends HttpServlet {

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

//        String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

            // Todo: Find the right sheet in the engine and return the SheetDTO.
            // For now I assume that there is only one sheet...

            String cellName = request.getParameter(Constants.CELL_NAME_PARAMETER);
            String cellValue = request.getParameter(Constants.CELL_VALUE_PARAMETER);

            engine.updateCellStatus(cellName, cellValue);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            // Todo: Handle correctly with the exception.
            response.setContentType("text/plain");
            response.getWriter().println("Something went wrong");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
