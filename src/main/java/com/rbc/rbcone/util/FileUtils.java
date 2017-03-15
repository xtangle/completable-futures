package com.rbc.rbcone.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class FileUtils {

    public static String readFromFile(String filename) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(filename);
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(in))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

}
