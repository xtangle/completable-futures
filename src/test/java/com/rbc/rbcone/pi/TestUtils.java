package com.rbc.rbcone.pi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;

final class TestUtils {

    private TestUtils() {}

    static class PiCalcFactory {
        private Class<? extends PiCalc> piCalcClass;

        PiCalcFactory(Class<? extends PiCalc> clazz) {
            piCalcClass = clazz;
        }

        PiCalc create(int decimalDigits) throws
                NoSuchMethodException, IllegalAccessException,
                InvocationTargetException, InstantiationException {
            return piCalcClass.getConstructor(int.class)
                    .newInstance(decimalDigits);
        }

        @Override
        public String toString() {
            return piCalcClass.getSimpleName();
        }
    }

    static String readFromFile(String filename) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(filename);
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(in))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
