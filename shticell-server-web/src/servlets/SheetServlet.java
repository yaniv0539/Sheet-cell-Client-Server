package servlets;

import com.google.gson.Gson;
import constants.Constants;
import dto.SheetDto;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Collection;

@WebServlet(name = "SheetServlet", urlPatterns = "/sheet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class SheetServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

            if (sheetName == null || sheetName.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new ServletException("Sheet name is required");
            }

            String sheetVersion = request.getParameter(Constants.SHEET_VERSION_PARAMETER);

            SheetDto sheetDTO;

            if (sheetVersion == null || sheetVersion.isEmpty()) {
                sheetDTO = engine.getSheetDTO(sheetName);
            } else {
                sheetDTO = engine.getSheetDTO(sheetName, Integer.parseInt(sheetVersion));
            }

            response.setContentType("application/json");
            response.getWriter().println(gson.toJson(sheetDTO));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            Collection<Part> parts = request.getParts();

            if (parts.size() != 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new ServletException("Expected exactly one part");
            }

            for (Part part : parts) {
                engine.addNewSheet(part.getInputStream());
            }

            response.setContentType("application/json");
            response.getWriter().println(gson.toJson(engine.getSheetDTO()));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException(e.getMessage());
        }
    }
}
