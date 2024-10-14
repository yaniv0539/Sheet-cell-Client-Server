package servlets;

import com.google.gson.Gson;
import dto.RangeDto;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "RangeServlet", urlPatterns = "/sheet/range")
public class RangeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String rangeName = request.getParameter("rangeName");

            if (rangeName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new ServletException("Range name is required");
            }

            RangeDto range = engine.getRangeDTO(rangeName);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(range));

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String rangeName = request.getParameter("rangeName");

            if (rangeName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new ServletException("Range name is required");
            }

            String rangeValue = request.getParameter("rangeValue");

            if (rangeValue == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new ServletException("Range value is required");
            }

            engine.addRange(rangeName, rangeValue);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(engine.getRanges()));

            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String rangeName = request.getParameter("rangeName");

            if (rangeName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new ServletException("Range name is required");
            }

            engine.deleteRange(rangeName);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(engine.getRanges()));

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(e.getMessage());
        }
    }
}
