package com.example.dt7syntaxcheck.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.dt7syntaxcheck.server.python.PythonExecutor;
import com.example.dt7syntaxcheck.server.python.PythonSyntaxChecker;

/**
 *
 * @author truon
 */
public class ServerMain {

    public static void main(String[] args) {
        PythonSyntaxChecker checker = new PythonSyntaxChecker();
        ErrorLog[] errors = checker.syntaxCheck("../temp/aa.py");
        if (errors.length == 0) {
            System.out.println("No syntax errors found.");
            PythonExecutor executor = new PythonExecutor();
            String output = executor.execute("temp/a.py");
            System.out.println("Program output:");
            System.out.println(output);
        } else
            for (ErrorLog log : errors) {
                System.out.println(log);
            }

    }

    public static void test(String[] args) {
        try (ServerSocket server = new ServerSocket(5000);) {
            System.out.println("Server is listening on port 5000");
            // blocking loop để chấp nhận kết nối từ client
            while (true) {
                Socket client = server.accept();
                String clientIdentify = "Client " + client.getRemoteSocketAddress();
                System.out.println(clientIdentify + " connected.");

                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Received from client: " + line);
                    writer.write("Echo: " + line + "\r\n");
                    writer.flush();
                    System.out.println("Sent to client: Echo: " + line);
                    if (line.equalsIgnoreCase("bye")) {
                        System.out.println(clientIdentify + " disconnected.");
                        client.close();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
