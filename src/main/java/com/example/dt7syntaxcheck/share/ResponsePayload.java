package com.example.dt7syntaxcheck.share;

import java.util.List;

public class ResponsePayload {

    private boolean isSuccess;      // true nếu code đúng syntax và chạy được
    private String output;          // Kết quả in ra màn hình hoặc thông báo lỗi gốc
    private String formattedCode;   // Code đã được căn lề đẹp (chỉ có khi isSuccess = true)
    private List<ErrorLog> errors;  // Danh sách các dòng lỗi (chỉ có khi isSuccess = false)

    public ResponsePayload(boolean isSuccess, String output, String formattedCode, List<ErrorLog> errors) {
        this.isSuccess = isSuccess;
        this.output = output;
        this.formattedCode = formattedCode;
        this.errors = errors;
    }

    // Getters
    public boolean isSuccess() {
        return isSuccess;
    }

    public String getOutput() {
        return output;
    }

    public String getFormattedCode() {
        return formattedCode;
    }

    public List<ErrorLog> getErrors() {
        return errors;
    }
}
