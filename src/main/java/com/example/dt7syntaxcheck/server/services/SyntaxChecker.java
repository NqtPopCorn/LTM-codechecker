package com.example.dt7syntaxcheck.server.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.dt7syntaxcheck.share.ErrorLog;

public class SyntaxChecker {

    public List<ErrorLog> parseErrors(String rawError, int languageId) {
        List<ErrorLog> errorLogs = new ArrayList<>();
        if (rawError == null || rawError.isEmpty()) {
            return errorLogs;
        }

        String[] lines = rawError.split("\n");
        List<Integer> processedLines = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            int lineNumber = -1;
            String errorMessage = "";

            Matcher m;
            switch (languageId) {
                case 62: // Java
                    m = Pattern.compile(":(\\d+):\\s*error:\\s*(.*)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = m.group(2);
                    }
                    break;

                case 54: // C++
                    m = Pattern.compile(":(\\d+):\\d+:\\s*error:\\s*(.*)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = m.group(2);
                    }
                    break;

                case 51: // C#
                    m = Pattern.compile("\\((\\d+),\\d+\\):\\s*error\\s+[^:]+:\\s*(.*)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = m.group(2);
                    }
                    break;

                case 71: // Python
                    m = Pattern.compile("line (\\d+)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = extractPythonOrJsError(lines); // Gọi hàm trích xuất thông minh
                    }
                    break;

                case 63: // JavaScript (Node.js)
                    // QUAN TRỌNG: Bỏ qua các dòng truy vết từ file nội bộ của NodeJS
                    if (line.contains("internal/") || line.contains("node:")) {
                        continue;
                    }

                    m = Pattern.compile("\\.js:(\\d+)").matcher(line);
                    Matcher m2 = Pattern.compile("evalmachine\\.<anonymous>:(\\d+)").matcher(line);

                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = extractPythonOrJsError(lines);
                    } else if (m2.find()) {
                        lineNumber = Integer.parseInt(m2.group(1));
                        errorMessage = extractPythonOrJsError(lines);
                    }
                    break;
            }

            // Nếu tìm thấy dòng lỗi (và chưa bị trùng)
            if (lineNumber != -1 && !processedLines.contains(lineNumber)) {
                // Viết hoa chữ cái đầu cho chuẩn form IDE
                if (!errorMessage.isEmpty()) {
                    errorMessage = errorMessage.substring(0, 1).toUpperCase() + errorMessage.substring(1);
                }
                errorLogs.add(new ErrorLog(lineNumber, 0, errorMessage));
                processedLines.add(lineNumber);
            }
        }

        // Fallback: Nếu không bóc tách được bằng Regex
        if (errorLogs.isEmpty() && lines.length > 0) {
            String fallbackMsg = lines[lines.length - 1].trim();
            // Đảm bảo không in ra những dòng "at internal/..." vô nghĩa
            if (fallbackMsg.contains("internal/") || fallbackMsg.contains("at ")) {
                fallbackMsg = extractPythonOrJsError(lines);
            }
            errorLogs.add(new ErrorLog(0, 0, fallbackMsg));
        }

        return errorLogs;
    }

    /**
     * Hàm hỗ trợ "Săn" thông báo lỗi chính xác cho Python và JavaScript Thay vì
     * lấy bừa dòng cuối, nó sẽ tìm dòng có chữ "Error"
     */
    private String extractPythonOrJsError(String[] lines) {
        // Quét ngược từ dưới lên để tìm dòng chứa chữ "Error:" (Vd: SyntaxError, ReferenceError)
        for (int i = lines.length - 1; i >= 0; i--) {
            String l = lines[i].trim();
            if (l.contains("Error:")) {
                return l; // Trả về câu thông báo lỗi chuẩn xác
            }
        }

        // Nếu không có chữ Error:, lấy dòng cuối cùng không phải là Stack Trace
        for (int i = lines.length - 1; i >= 0; i--) {
            String l = lines[i].trim();
            if (!l.isEmpty() && !l.startsWith("at ") && !l.contains("internal/")) {
                return l;
            }
        }

        return "Lỗi cú pháp (Syntax Error)"; // Thông báo mặc định an toàn
    }
}
