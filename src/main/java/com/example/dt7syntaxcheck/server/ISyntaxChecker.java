package com.example.dt7syntaxcheck.server;

import com.example.dt7syntaxcheck.server.ErrorLog;

/**
 *
 * @author truon
 */
public interface ISyntaxChecker {
    ErrorLog[] syntaxCheck(String filePath);
}
