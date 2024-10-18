package servlets.logical.sort.numeric.colimns;

import com.google.gson.Gson;
import dto.BoundariesDto;
import dto.CoordinateDto;
import dto.SortDto;
import sheet.coordinate.impl.CoordinateImpl;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesFactory;
import utils.Constants;
import engine.api.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "NumericColumnsServlet", urlPatterns = "/sheet/sort/numericColumns")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class NumericColumnsServlet  extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

            String boundaries = request.getParameter(Constants.RANGE_BOUNDARIES_PARAMETER);

            if (boundaries == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new RuntimeException("Boundaries name is required");
            }

            BoundariesDto boundariesDto = engine.getBoundaries(sheetName, boundaries);
            CoordinateDto fromDto = boundariesDto.getFrom();
            CoordinateDto toDto = boundariesDto.getTo();

            CoordinateImpl from = CoordinateImpl.create(fromDto.getRow(), fromDto.getColumn());
            CoordinateImpl to = CoordinateImpl.create(toDto.getRow(), toDto.getColumn());

            Boundaries boundaries1 = BoundariesFactory.createBoundaries(from, to);

            int version = Integer.parseInt(sheetVersion);

            List<String> numericColumnsInRange = engine.getNumericColumnsInRange(sheetName, boundaries1, version);

            SortDto sortDto = new SortDto(boundariesDto, numericColumnsInRange);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(sortDto));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
