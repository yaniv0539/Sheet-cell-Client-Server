package servlets;

import com.google.gson.Gson;
import constants.Constants;
import dto.CellDto;
import dto.SheetDto;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "CellServlet", urlPatterns = {"/sheet/cell"})
public class CellServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);
            SheetDto sheetDTO = engine.getSheetDTO(sheetName);

            if (sheetName == null || sheetDTO == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new ServletException("Invalid sheet name");
            }

            String cellName = request.getParameter(Constants.CELL_NAME_PARAMETER);
            CellDto cellDto = sheetDTO.activeCells.get(cellName);

            if (cellName == null || cellDto == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new ServletException("Invalid cell name");
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(cellDto));
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

            String cellName = request.getParameter(Constants.CELL_NAME_PARAMETER);

            StringBuilder body = new StringBuilder();
            String line;

            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            engine.updateCell(sheetName, cellName, body.toString());
            SheetDto sheetDTO = engine.getSheetDTO(sheetName);
            response.getWriter().print(gson.toJson(sheetDTO));
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
