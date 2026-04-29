package com.example.backend.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.regex.Pattern;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:(.*?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe(.*?)>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE)
    };

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) return null;

        String[] sanitizedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitizedValues[i] = sanitize(values[i]);
        }
        return sanitizedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return sanitize(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return sanitize(value);
    }

    private String sanitize(String value) {
        if (value == null) return null;

        String sanitized = value;
        for (Pattern pattern : XSS_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }

        return sanitized.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}