package com.brightpath;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Lets a pipeline confirm which build is live for which customer after a deploy. */
@WebServlet("/version")
public class VersionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().write(String.format(
                "{\"version\":\"%s\",\"gitSha\":\"%s\",\"customer\":\"%s\"}",
                AppConfig.version(), AppConfig.gitSha(), AppConfig.customerName()));
    }
}
