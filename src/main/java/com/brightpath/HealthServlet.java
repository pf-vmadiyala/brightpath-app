package com.brightpath;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * /health      liveness: Tomcat is up and serving (no DB call).
 * /health/db   readiness: can reach the database. Point the ALB / target group
 *              health check at /health so a DB blip doesn't drain every task;
 *              use /health/db in post-deploy smoke tests.
 */
@WebServlet(urlPatterns = {"/health", "/health/db"})
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        if (!"/health/db".equals(req.getServletPath())) {
            resp.getWriter().write("{\"status\":\"ok\"}");
            return;
        }

        try (Connection conn = AppConfig.connect(); Statement st = conn.createStatement()) {
            st.execute("SELECT 1");
            resp.getWriter().write("{\"status\":\"ok\",\"db\":\"up\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("{\"status\":\"error\",\"db\":\"down\"}");
            log("DB health check failed", e);
        }
    }
}
