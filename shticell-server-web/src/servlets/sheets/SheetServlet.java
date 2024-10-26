package servlets.sheets;

import com.google.gson.Gson;
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

            String userName = ServletUtils.getUserName(request);
            String sheetName = ServletUtils.getSheetName(request);
            int sheetVersion = ServletUtils.getSheetVersion(request);

            SheetDto sheetDTO;

            if (sheetVersion == 0) {
                sheetDTO = engine.getSheetDTO(userName, sheetName);
            } else {
                sheetDTO = engine.getSheetDTO(userName, sheetName, sheetVersion);
            }

            response.setContentType("application/json");
            response.getWriter().println(gson.toJson(sheetDTO));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String userName = ServletUtils.getUserName(request);
            String sheetName = null;

            Collection<Part> parts = request.getParts();

            if (parts.size() != 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Expected exactly one part");
            }


                for (Part part : parts) {

                    sheetName = engine.addNewSheet(userName, part.getInputStream());
                }

            // TODO: Change from beginner. done be itay 25/10
            //sheet name should never be null
            SheetDto sheetDTO = engine.getSheetDTO(userName, sheetName);

            response.setContentType("text/plain");
            response.getWriter().println(gson.toJson(sheetDTO));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
