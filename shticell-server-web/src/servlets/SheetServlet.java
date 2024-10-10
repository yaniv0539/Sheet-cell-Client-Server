package servlets;

import com.google.gson.Gson;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import sheet.api.SheetGetters;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Collection;

@WebServlet(name = "SheetServlet", urlPatterns = "/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class SheetServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

//        String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

            // Todo: Find the right sheet in the engine and return the SheetDTO.
            // For now I assume that there is only one sheet...

            // Todo: Replace SheetGetters to SheetDTO when Itay finish doing it.
            SheetGetters sheetStatus = engine.getSheetStatus();

            response.setContentType("application/json");
            response.getWriter().println(gson.toJson(sheetStatus));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            // Todo: Handle correctly with the exception.
            response.setContentType("text/plain");
            response.getWriter().println("Something went wrong");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            // Todo: Store the XML in the engine.
            // For now it's just update the current sheet.
            Collection<Part> parts = request.getParts();

            if (parts.size() != 1) {
                throw new ServletException("Expected exactly one part");
            }

            for (Part part : parts) {
                engine.readXMLInitFile(part.getInputStream());
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(gson.toJson(engine.getSheetDTOStatus()));

        } catch (Exception e) {
            // Todo: Handle correctly with the exception.
            response.setContentType("text/plain");
            response.getWriter().println("Something went wrong");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
