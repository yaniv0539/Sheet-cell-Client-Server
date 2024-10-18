package servlets.logical.sort;

import com.google.gson.Gson;
import utils.Constants;
import dto.*;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheet.coordinate.impl.CoordinateImpl;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesFactory;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SortServlet", urlPatterns = "/sheet/sort")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class SortServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                throw new RuntimeException("Sheet version is required");
            }

            StringBuilder jsonBody = new StringBuilder();
            String line;

            while ((line = request.getReader().readLine()) != null) {
                jsonBody.append(line);
            }

            SortDto sortDto = gson.fromJson(jsonBody.toString(), SortDto.class);

            BoundariesDto boundariesDto = sortDto.getBoundariesDto();
            CoordinateDto fromDto = boundariesDto.getFrom();
            CoordinateDto toDto = boundariesDto.getTo();

            CoordinateImpl from = CoordinateImpl.create(fromDto.getRow(), fromDto.getColumn());
            CoordinateImpl to = CoordinateImpl.create(toDto.getRow(), toDto.getColumn());

            Boundaries boundaries = BoundariesFactory.createBoundaries(from, to);
            List<String> sortByColumns = sortDto.getSortByColumns();
            int version = Integer.parseInt(sheetVersion);

            List<List<CoordinateDto>> lists = engine.sortCellsInRange(sheetName, boundaries, sortByColumns, version);
            SheetDto sortSheet = engine.sort(sheetName, boundaries, sortByColumns, version);

            SortDesignDto sortDesignDto = new SortDesignDto(sortSheet, boundariesDto, lists);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(sortDesignDto));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
