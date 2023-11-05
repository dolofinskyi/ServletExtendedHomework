package app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private DateTimeFormatter formatter;
    private static final String DEFAULT_TIMEZONE = "UTC";
    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        engine = new TemplateEngine();
        createThymeleafClassLoaderResolver("templates/", ".html", TemplateMode.HTML);
    }

    private void createThymeleafClassLoaderResolver(String prefix, String suffix, TemplateMode mode) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(prefix);
        resolver.setSuffix(suffix);
        resolver.setTemplateMode(mode);
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String paramTimezone = req.getParameter("timezone");
        String lastTimezone = getCookieValue(req, "lastTimezone");
        ZonedDateTime zonedTime = createTime(paramTimezone, lastTimezone);
        String time = zonedTime.format(formatter);

        Map<String, Object> params = Map.of("time", time);
        Context context = new Context(req.getLocale(), Map.of("params", params));

        resp.setContentType("text/html");
        if (paramTimezone != null) {
            resp.addCookie(new Cookie("lastTimezone", zonedTime.getZone().toString()));
        }
        engine.process("time", context, resp.getWriter());
        resp.getWriter().close();
    }

    private String getCookieValue(HttpServletRequest req, String name) {
        if (req.getCookies() == null) {
            return null;
        }
        for (Cookie cookie: req.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ZonedDateTime createTime(String paramTimezone, String lastTimezone) {
        String timezone = paramTimezone != null ?
                paramTimezone.replace(' ', '+') :
                DEFAULT_TIMEZONE;
        if (lastTimezone != null && paramTimezone == null) {
            return getTimeWithZone(lastTimezone);
        }
        return getTimeWithZone(timezone);
    }

    private ZonedDateTime getTimeWithZone(String zone) {
        return Instant.now().atZone(ZoneId.of(zone));
    }
}
