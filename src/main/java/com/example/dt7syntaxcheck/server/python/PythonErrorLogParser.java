package com.example.dt7syntaxcheck.server.python;

import com.example.dt7syntaxcheck.server.ErrorLog;
import com.example.dt7syntaxcheck.server.IErrorLogParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonErrorLogParser implements IErrorLogParser {

    // Matches lines like: \a.py:2:6 - error: "(" was not closed
    private static final Pattern ERROR_PATTERN = Pattern.compile(".*:(\\d+):(\\d+)\\s+-\\s+error:\\s+(.+)");

    @Override
    public ErrorLog parse(String text) {
        Matcher m = ERROR_PATTERN.matcher(text.trim());
        if (m.matches()) {
            int line = Integer.parseInt(m.group(1));
            int index = Integer.parseInt(m.group(2));
            String msg = m.group(3).trim();
            return new ErrorLog(line, index, msg);
        }
        return null;
    }

}
