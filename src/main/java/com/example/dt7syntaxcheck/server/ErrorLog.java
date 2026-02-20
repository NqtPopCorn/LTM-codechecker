/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.dt7syntaxcheck.server;

/**
 *
 * @author truon
 */
public class ErrorLog {
    int line;
    int index;
    String message;

    public ErrorLog(int line, int index, String message) {
        this.line = line;
        this.index = index;
        this.message = message;
    }
}
