package filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;

@WebFilter("/time")
public class TimezoneValidateFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (req.getParameter("timezone") == null) {
            chain.doFilter(req, res);
            return;
        }

        // UTC+3 decoded to UTC 3
        String timeZone = req.getParameter("timezone")
                .replace(' ', '+');
        try {
            ZoneId.of(timeZone);
            chain.doFilter(req, res);
        } catch (DateTimeException e) {
            res.setStatus(400);
            res.getWriter().write("Invalid timezone");
            res.getWriter().close();
        }
    }
}
