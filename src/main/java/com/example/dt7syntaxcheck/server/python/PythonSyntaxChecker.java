package com.example.dt7syntaxcheck.server.python;

import com.example.dt7syntaxcheck.server.ErrorLog;
import com.example.dt7syntaxcheck.server.ISyntaxChecker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PythonSyntaxChecker implements ISyntaxChecker {

    private final PythonErrorLogParser parser = new PythonErrorLogParser();

    @Override
    public ErrorLog[] syntaxCheck(String filePath) {
        String output = executeCMD(filePath);
        List<ErrorLog> errors = new ArrayList<>();
        for (String line : output.split("\n")) {
            ErrorLog log = parser.parse(line);
            if (log != null) {
                errors.add(log);
            }
        }
        return errors.toArray(new ErrorLog[0]);
    }

    private String executeCMD(String filePath) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "-m", "pyright", filePath)
                    .redirectErrorStream(true); // gộp stderr vào stdout để đọc lỗi từ pyright

            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Failed to run pyright: " + e.getMessage(), e);
        }
        return output.toString();
    }

}
