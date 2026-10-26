package com.rental;

/**
 * Entry point of the Rental Tracker CLI.
 *
 * <p>Later this class becomes the "composition root": the single place where we
 * create the Database, Repositories, Services and the Menu and hand them to each
 * other. For now it only proves that the build works.
 */
public class App {

    /** Returns the banner text (kept separate from main() so it is easy to test). */
    public static String banner() {
        return "=== Rental tracker ===";
    }

    /** JVM calls this method first when the program starts. */
    public static void main(String[] args) {
        System.out.println(banner());
    }
}