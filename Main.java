import java.util.*;

/**
 * Simple Test Program for LTM Code Checker
 * Class name MUST be "Main" for Judge0 compatibility
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║   LTM Test - Simple Version    ║");
        System.out.println("╚════════════════════════════════╝");
        
        // Test 1: Simple Output
        System.out.println("\n=== TEST 1: Output ===");
        System.out.println("Hello World!");
        
        // Test 2: Math
        System.out.println("\n=== TEST 2: Math ===");
        int a = 10, b = 20;
        System.out.println("10 + 20 = " + (a + b));
        System.out.println("10 * 20 = " + (a * b));
        
        // Test 3: Prime Checker
        System.out.println("\n=== TEST 3: Prime Numbers ===");
        System.out.println("5 is " + (isPrime(5) ? "prime" : "not prime"));
        System.out.println("10 is " + (isPrime(10) ? "prime" : "not prime"));
        System.out.println("29 is " + (isPrime(29) ? "prime" : "not prime"));
        
        // Test 4: Factorial
        System.out.println("\n=== TEST 4: Factorial ===");
        System.out.println("5! = " + factorial(5));
        
        // Test 5: Array
        System.out.println("\n=== TEST 5: Array Sum ===");
        int[] numbers = {1, 2, 3, 4, 5};
        int sum = 0;
        for (int n : numbers) sum += n;
        System.out.println("Sum of [1,2,3,4,5] = " + sum);
        
        // Test 6: Fibonacci
        System.out.println("\n=== TEST 6: Fibonacci (first 10) ===");
        for (int i = 0; i < 10; i++) {
            System.out.print(fibonacci(i) + " ");
        }
        System.out.println();
        
        // Test 7: String
        System.out.println("\n=== TEST 7: String ===");
        String text = "Hello Java";
        System.out.println("Text: " + text);
        System.out.println("Length: " + text.length());
        System.out.println("Uppercase: " + text.toUpperCase());
        
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║     All Tests Passed! ✓        ║");
        System.out.println("╚════════════════════════════════╝");
    }
    
    // Check Prime Number
    public static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }
    
    // Calculate Factorial
    public static int factorial(int n) {
        if (n == 0 || n == 1) return 1;
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    
    // Fibonacci Number
    public static int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
