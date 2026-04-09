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

        Pattern pattern = null;

        switch (languageId) {
            case 71: // Python 
                pattern = Pattern.compile("line (\\d+)");
                break;
            case 62: // Java
            case 54: // C++
            case 51: // C# 
                pattern = Pattern.compile(":(\\d+):");
                break;
            case 63: // JavaScript
                pattern = Pattern.compile("evalmachine\\.<anonymous>:(\\d+)");
                break;
        }

        if (pattern != null) {
            Matcher matcher = pattern.matcher(rawError);
            while (matcher.find()) {
                int lineNumber = Integer.parseInt(matcher.group(1));
                String hint = generateHint(rawError);
                errorLogs.add(new ErrorLog(lineNumber, 0, "Lỗi cú pháp tại dòng này. " + hint));
            }
        }
        return errorLogs;
    }

    private String generateHint(String rawError) {
        String errorLower = rawError.toLowerCase();
        if (errorLower.contains("expected ';'") || errorLower.contains("missing ';'")) {
            return "Gợi ý: Bạn có quên dấu chấm phẩy ';' ở cuối câu lệnh không?";
        }
        if (errorLower.contains("was not declared")) {
            return "Gợi ý: Biến hoặc hàm này chưa được khai báo.";
        }
        if (errorLower.contains("invalid syntax")) {
            return "Gợi ý: Kiểm tra lại cấu trúc lệnh, có thể thiếu dấu hai chấm ':' hoặc ngoặc.";
        }
        return "Gợi ý: Hãy kiểm tra kỹ lại chính tả và các dấu ngoặc đóng/mở.";
    }
}
