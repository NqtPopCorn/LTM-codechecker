# Syntax Checker — Developer Guide

This document describes the CLI commands used by the server to perform syntax checking and code execution for each supported language.

---

## Table of Contents

- [Python](#python)
- [Java](#java)
- [C++](#c)
- [JavaScript](#javascript)
- [C#](#c-1)

---

## Python

**Prerequisite:** `pyright` must be installed (`pip install pyright`).

### Syntax Check

```ps
python -m pyright a.py
```

**Example output:**

```
  \a.py:2:6 - error: "(" was not closed
  \a.py:2:7 - error: String literal is unterminated
  \a.py:3:20 - error: Statements must be separated by newlines or semicolons
3 errors, 0 warnings, 0 informations
```

### Execute

```ps
python a.py
```

---

## Java

**Prerequisite:** JDK must be installed and `java` must be on the system PATH.

> Note: The same `java` command is used for both syntax checking and execution.

### Syntax Check / Execute

```ps
java TestSyntaxChecking.java
```

**Example output (syntax error):**

```
TestSyntaxChecking.java:7: error: unclosed string literal
        System.out.println("This line has a syntax error);
                           ^
TestSyntaxChecking.java:9: error: unclosed string literal
        System.out.println("This line has a syntax error);
                           ^
2 errors
error: compilation failed
```

---

## C++

**Prerequisite:** GCC (`g++`) must be installed and available on the system PATH.

### Syntax Check

```ps
g++ -c test.cpp
```

**Example output (syntax error):**

```
test.cpp:6:18: warning: missing terminating " character
std::cout << "Hello, World! << std::endl;
^
test.cpp:6:5: error: missing terminating " character
std::cout << "Hello, World! << std::endl;
^
test.cpp:8:18: warning: missing terminating " character
<< "Hello, World! << std::endl;
^
test.cpp:8:15: error: missing terminating " character
<< "Hello, World! << std::endl;
^
test.cpp: In function 'int main()':
test.cpp:9:9: error: expected primary-expression before 'return'
return 0;
^
```

### Execute

```ps
g++ a.cpp -o main.exe
./main.exe
```

---

## JavaScript

**Prerequisite:** TypeScript compiler (`tsc`) must be installed (`npm install -g typescript`). Node.js is required for execution.

### Syntax Check

```ps
tsc test.js --allowJs --noEmit
```

**Example output (syntax error):**

```
test.js:1:29 - error TS1002: Unterminated string literal.

1 console.log("Hello, World!);

test.js:2:30 - error TS1002: Unterminated string literal.

2 ,console.log("Hello, World!);

Found 2 errors in the same file, starting at: test.js:1
```

### Execute

```ps
node test.js
```

---

## C#

**Prerequisite:** `csc` (Roslyn C# compiler, bundled with Visual Studio) must be on the system PATH.

> Setup: Add the Roslyn compiler directory to your environment PATH variable.
> Default path: `C:\Program Files\Microsoft Visual Studio\2022\Community\MSBuild\Current\Bin\Roslyn`

### Syntax Check / Compile

```ps
csc Program.cs
```

**Example output (error compile):**

```
Microsoft (R) Visual C# Compiler version 4.13.0-3.25167.3 (73eff2b5)
Copyright (C) Microsoft Corporation. All rights reserved.

Program.cs(7,27): error CS1010: Newline in constant
Program.cs(7,43): error CS1026: ) expected
Program.cs(7,43): error CS1002: ; expected
```

### Execute (after running csc)

```ps
Program.exe
```
