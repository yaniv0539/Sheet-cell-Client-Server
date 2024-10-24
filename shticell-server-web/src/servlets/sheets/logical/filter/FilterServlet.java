package servlets.sheets.logical.filter;

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
import java.util.Map;

@WebServlet(name = "FilterServlet", urlPatterns = "/sheet/filter")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FilterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Engine engine = ServletUtils.getEngine(getServletContext());
            Gson gson = ServletUtils.getGson(getServletContext());

            String userName = ServletUtils.getUserName(request);
            String sheetName = ServletUtils.getSheetName(request);
            int sheetVersion = ServletUtils.getSheetVersion(request);

            String jsonBody = ServletUtils.getJsonBody(request);

            FilterDto filterDto = gson.fromJson(jsonBody, FilterDto.class);

            BoundariesDto boundariesDto = filterDto.boundariesDto();
            CoordinateDto fromDto = boundariesDto.from();
            CoordinateDto toDto = boundariesDto.to();

            CoordinateImpl from = CoordinateImpl.create(fromDto.row(), fromDto.column());
            CoordinateImpl to = CoordinateImpl.create(toDto.row(), toDto.column());

            Boundaries boundaries = BoundariesFactory.createBoundaries(from, to);
            String filterByColumn = filterDto.filterByColumn();
            List<String> byValues = filterDto.byValues();

            Map<CoordinateDto, CoordinateDto> coordinateCoordinateMap = engine.filteredMap(userName, sheetName, boundaries, filterByColumn, byValues, sheetVersion);
            SheetDto filterSheet = engine.filter(userName, sheetName, boundaries, filterByColumn, byValues, sheetVersion);

            FilterDesignDto filterDesignDto = new FilterDesignDto(filterSheet, coordinateCoordinateMap, boundariesDto);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(filterDesignDto));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
