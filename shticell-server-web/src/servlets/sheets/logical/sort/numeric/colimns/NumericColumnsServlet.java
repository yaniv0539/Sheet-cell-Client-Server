package servlets.sheets.logical.sort.numeric.colimns;

import com.google.gson.Gson;
import dto.BoundariesDto;
import dto.CoordinateDto;
import dto.SortDto;
import sheet.coordinate.impl.CoordinateImpl;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesFactory;
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

            String userName = ServletUtils.getUserName(request);
            String sheetName = ServletUtils.getSheetName(request);
            int sheetVersion = ServletUtils.getSheetVersion(request);
            String boundaries = ServletUtils.getBoundaries(request);

            BoundariesDto boundariesDto = engine.getBoundaries(userName, sheetName, boundaries);
            CoordinateDto fromDto = boundariesDto.from();
            CoordinateDto toDto = boundariesDto.to();

            CoordinateImpl from = CoordinateImpl.create(fromDto.row(), fromDto.column());
            CoordinateImpl to = CoordinateImpl.create(toDto.row(), toDto.column());

            Boundaries boundaries1 = BoundariesFactory.createBoundaries(from, to);

            List<String> numericColumnsInRange = engine.getNumericColumnsInRange(userName, sheetName, boundaries1, sheetVersion);

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
