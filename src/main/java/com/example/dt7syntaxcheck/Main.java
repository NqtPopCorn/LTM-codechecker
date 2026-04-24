package com.example.dt7syntaxcheck;

import com.example.dt7syntaxcheck.client.ClientUIFrame;
import com.example.dt7syntaxcheck.server.ServerMain;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("server")) {
            ServerMain.main(args);
        } else {
            ClientUIFrame.main(args);
        }
    }
}
