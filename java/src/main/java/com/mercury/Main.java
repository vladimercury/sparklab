package com.mercury;

public class Main {
    public static void main(String[] args) {
        String inputUrl = args[0];
        String outputUrl = args[1];

        new LogProcessor(inputUrl, outputUrl).doWork();
    }
}
