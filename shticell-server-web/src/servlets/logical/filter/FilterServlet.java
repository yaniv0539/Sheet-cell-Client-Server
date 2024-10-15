package servlets.logical.filter;

import com.google.gson.Gson;
import constants.Constants;
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

            String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

            if (sheetName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Sheet name is required");
            }

            String sheetVersion = request.getParameter(Constants.SHEET_VERSION_PARAMETER);

            if (sheetVersion == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Range name is required");
            }

            StringBuilder jsonBody = new StringBuilder();
            String line;

            while ((line = request.getReader().readLine()) != null) {
                jsonBody.append(line);
            }

            FilterDto filterDto = gson.fromJson(jsonBody.toString(), FilterDto.class);

            BoundariesDto boundariesDto = filterDto.getBoundariesDto();
            CoordinateDto fromDto = boundariesDto.getFrom();
            CoordinateDto toDto = boundariesDto.getTo();

            CoordinateImpl from = CoordinateImpl.create(fromDto.getRow(), fromDto.getColumn());
            CoordinateImpl to = CoordinateImpl.create(toDto.getRow(), toDto.getColumn());

            Boundaries boundaries = BoundariesFactory.createBoundaries(from, to);
            String filterByColumn = filterDto.getFilterByColumn();
            List<String> byValues = filterDto.getByValues();
            int version = Integer.parseInt(sheetVersion);

            Map<CoordinateDto, CoordinateDto> coordinateCoordinateMap = engine.filteredMap(sheetName, boundaries, filterByColumn, byValues, version);
            SheetDto filterSheet = engine.filter(sheetName, boundaries, filterByColumn, byValues, version);

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
