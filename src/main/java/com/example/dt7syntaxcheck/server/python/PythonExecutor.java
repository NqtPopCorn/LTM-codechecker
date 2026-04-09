package com.example.dt7syntaxcheck.server.python;

import com.example.dt7syntaxcheck.server.ICodeExecutor;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonExecutor implements ICodeExecutor {

    @Override
    public String execute(String filePath) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", filePath)
                    // .directory(null)
                    .redirectErrorStream(true); // gộp stderr vào stdout để đọc lỗi từ python
                    
                
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
