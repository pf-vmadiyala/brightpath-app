package com.brightpath;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Lists courses and handles the "add course" and "enroll" forms. */
@WebServlet("/courses")
public class CourseServlet extends HttpServlet {

    private static final String LIST_SQL =
            "SELECT c.id, c.title, c.category, COUNT(e.id) AS enrolled "
          + "FROM courses c LEFT JOIN enrollments e ON e.course_id = c.id "
          + "GROUP BY c.id, c.title, c.category ORDER BY c.id";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Map<String, Object>> courses = new ArrayList<>();
        try (Connection conn = AppConfig.connect();
             PreparedStatement st = conn.prepareStatement(LIST_SQL);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                courses.add(Map.of(
                        "id", rs.getLong("id"),
                        "title", rs.getString("title"),
                        "category", rs.getString("category"),
                        "enrolled", rs.getLong("enrolled")));
            }
        } catch (SQLException e) {
            throw new ServletException("Could not load courses", e);
        }

        req.setAttribute("courses", courses);
        req.setAttribute("customer", AppConfig.customerName());
        req.setAttribute("version", AppConfig.version());
        req.getRequestDispatcher("/WEB-INF/courses.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try (Connection conn = AppConfig.connect()) {
            if ("enroll".equals(action)) {
                try (PreparedStatement st = conn.prepareStatement(
                        "INSERT INTO enrollments (course_id, student_email) VALUES (?, ?)")) {
                    st.setLong(1, Long.parseLong(req.getParameter("courseId")));
                    st.setString(2, req.getParameter("email"));
                    st.executeUpdate();
                }
            } else {
                try (PreparedStatement st = conn.prepareStatement(
                        "INSERT INTO courses (title, category) VALUES (?, ?)")) {
                    st.setString(1, req.getParameter("title"));
                    st.setString(2, blankToDefault(req.getParameter("category")));
                    st.executeUpdate();
                }
            }
        } catch (SQLException | NumberFormatException e) {
            throw new ServletException("Could not save", e);
        }
        resp.sendRedirect(req.getContextPath() + "/courses");
    }

    private static String blankToDefault(String category) {
        return (category == null || category.isBlank()) ? "general" : category.trim();
    }
}
