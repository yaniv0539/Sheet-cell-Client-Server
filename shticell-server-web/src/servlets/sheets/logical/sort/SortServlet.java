package servlets.sheets.logical.sort;

import com.google.gson.Gson;
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

            String userName = ServletUtils.getUserName(request);
            String sheetName = ServletUtils.getSheetName(request);
            int sheetVersion = ServletUtils.getSheetVersion(request);

            String jsonBody = ServletUtils.getJsonBody(request);

            SortDto sortDto = gson.fromJson(jsonBody, SortDto.class);

            BoundariesDto boundariesDto = sortDto.boundariesDto();
            CoordinateDto fromDto = boundariesDto.from();
            CoordinateDto toDto = boundariesDto.to();

            CoordinateImpl from = CoordinateImpl.create(fromDto.row(), fromDto.column());
            CoordinateImpl to = CoordinateImpl.create(toDto.row(), toDto.column());

            Boundaries boundaries = BoundariesFactory.createBoundaries(from, to);
            List<String> sortByColumns = sortDto.sortByColumns();

            List<List<CoordinateDto>> lists = engine.sortCellsInRange(userName, sheetName, boundaries, sortByColumns, sheetVersion);
            SheetDto sortSheet = engine.sort(userName, sheetName, boundaries, sortByColumns, sheetVersion);

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
