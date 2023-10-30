package app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private DateTimeFormatter formatter;
    private static final String DEFAULT_TIMEZONE = "UTC";

    @Override
    public void init() throws ServletException {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String paramTimeZone = req.getParameter("timezone");
        String time;

        if (paramTimeZone == null) {
            time = createTime(DEFAULT_TIMEZONE);
        } else {
            time = createTime(paramTimeZone.replace(' ', '+'));
        }

        resp.getWriter().write(time);
        resp.getWriter().close();
    }


    public String createTime(String zone) {
        return Instant.now().atZone(ZoneId.of(zone)).format(formatter);
    }
}
