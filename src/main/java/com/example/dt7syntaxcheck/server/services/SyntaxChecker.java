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

        // Tách chuỗi lỗi gốc thành từng dòng để dễ dàng quét (scan) giống VS Code
        String[] lines = rawError.split("\n");
        List<Integer> processedLines = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            int lineNumber = -1;
            String errorMessage = "";

            Matcher m;
            switch (languageId) {
                case 62: // Java (Định dạng: File.java:X: error: <Thông_báo_lỗi>)
                    m = Pattern.compile(":(\\d+):\\s*error:\\s*(.*)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = m.group(2); // Lấy trực tiếp thông báo lỗi của JDK
                    }
                    break;

                case 54: // C++ (Định dạng: File.cpp:X:Y: error: <Thông_báo_lỗi>)
                    m = Pattern.compile(":(\\d+):\\d+:\\s*error:\\s*(.*)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = m.group(2); // Lấy trực tiếp thông báo lỗi của GCC
                    }
                    break;

                case 51: // C# (Định dạng: File.cs(X,Y): error CSXXXX: <Thông_báo_lỗi>)
                    m = Pattern.compile("\\((\\d+),\\d+\\):\\s*error\\s+[^:]+:\\s*(.*)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = m.group(2); // Lấy trực tiếp thông báo lỗi của Roslyn
                    }
                    break;

                case 71: // Python
                    // Python hiển thị dòng lỗi ở trên, thông báo lỗi ở cuối cùng
                    m = Pattern.compile("line (\\d+)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = lines[lines.length - 1].trim(); // Lấy dòng cuối của Traceback
                    }
                    break;

                case 63: // JavaScript (Node.js)
                    m = Pattern.compile("\\.js:(\\d+)").matcher(line);
                    Matcher m2 = Pattern.compile("evalmachine\\.<anonymous>:(\\d+)").matcher(line);
                    if (m.find()) {
                        lineNumber = Integer.parseInt(m.group(1));
                        errorMessage = lines[lines.length - 1].trim();
                    } else if (m2.find()) {
                        lineNumber = Integer.parseInt(m2.group(1));
                        errorMessage = lines[lines.length - 1].trim();
                    }
                    break;
            }

            // Nếu tìm thấy dòng lỗi và dòng này chưa được báo cáo
            if (lineNumber != -1 && !processedLines.contains(lineNumber)) {
                // Tự động viết hoa chữ cái đầu tiên cho chuẩn form IDE
                if (!errorMessage.isEmpty()) {
                    errorMessage = errorMessage.substring(0, 1).toUpperCase() + errorMessage.substring(1);
                }

                // Add vào danh sách ErrorLog gửi về Client (không kèm chữ Gợi ý)
                errorLogs.add(new ErrorLog(lineNumber, 0, errorMessage));
                processedLines.add(lineNumber);
            }
        }

        // Fallback: Đề phòng trường hợp lỗi lạ (không khớp Regex)
        // Hệ thống sẽ trả về dòng cuối cùng của chuỗi lỗi để Client không bị "mù" thông tin
        if (errorLogs.isEmpty() && lines.length > 0) {
            String fallbackMsg = lines[lines.length - 1].trim();
            errorLogs.add(new ErrorLog(0, 0, fallbackMsg));
        }

        return errorLogs;
    }
}
