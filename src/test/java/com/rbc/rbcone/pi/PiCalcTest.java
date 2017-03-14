package com.rbc.rbcone.pi;

import com.rbc.rbcone.pi.TestUtils.PiCalcFactory;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PiCalcTest {

    private static BigDecimal PI;
    private static final String PI_FILE = "pi/pi-million.txt";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss.SSS");

    private static final Object[][] MACHIN_TEST = {{new PiCalcFactory(Machin.class)}};
    private static final Object[][] MACHIN_PARALLEL_TEST = {{new PiCalcFactory(MachinParallel.class)}};
    private static final Object[][] CHUDNOVSKY_TEST = {{new PiCalcFactory(Chudnovsky.class)}};
    private static final Object[][] BRENT_SALAMIN_TEST = {{new PiCalcFactory(BrentSalamin.class)}};

    @SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
    private static final Object[][] ALL_PI_CALC_TEST = new Object[][]{
            MACHIN_TEST[0],
            MACHIN_PARALLEL_TEST[0],
            CHUDNOVSKY_TEST[0],
            BRENT_SALAMIN_TEST[0],
    };

    @SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
    private static final Object[][] CUSTOM_TEST = new Object[][]{
            MACHIN_TEST[0],
    };

    /**
     * Specify your test here
     */
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> implFactories() {
        return Arrays.asList(CHUDNOVSKY_TEST);
    }

    static {
        try {
            System.out.println(String.format("Reading value of pi from %s ...", PI_FILE));
            String piString = TestUtils.readFromFile(PI_FILE);
            PI = new BigDecimal(piString);
            System.out.println("Finished reading from file\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Parameterized.Parameter
    public PiCalcFactory piCalcFactory;

    @Rule
    public TestRule watcher = new TestWatcher() {

        @Override
        protected void starting(Description description) {
            System.out.println(String.format("Started test: %s at %s",
                    description.getMethodName(),
                    SDF.format(new Date(System.currentTimeMillis()))));
        }

        @Override
        protected void finished(Description description) {
            System.out.println(String.format("Finished test: %s at %s\n",
                    description.getMethodName(),
                    SDF.format(new Date(System.currentTimeMillis()))));
        }
    };

    @Test
    public void test01_Compute_First_100_Digits() {
        IntStream.rangeClosed(0, 100).parallel()
                .forEach(i -> verifyCompute(i, false));
    }

    @Test
    public void test02_Compute_1K_Digits() {
        verifyCompute(1_000);
    }

    @Test
    public void test03_Compute_10K_Digits() {
        verifyCompute(10_000);
    }

    @Test
    public void test04_Compute_100K_Digits() {
        verifyCompute(100_000);
    }

    @Test
    public void test05_Compute_500K_Digits() {
        verifyCompute(500_000);
    }

    @Test
    public void test06_Compute_1M_Digits() {
        verifyCompute(1_000_000);
    }

    @Test
    @Ignore
    public void test07_Compute_100M_Digits() {
        profileCompute(createPiCalc(100_000_000));
    }

    @Test
    @Ignore
    public void test08_Compute_Random_Digits_100K_500K() {
        verifyComputeRandomDigits(100_001, 499_999);
    }

    @Test
    @Ignore
    public void test09_Compute_Random_Digits_500K_1M() {
        verifyComputeRandomDigits(500_001, 999_999);
    }

    private BigDecimal profileCompute(PiCalc fixture) {
        long startTime, finishTime;
        startTime = System.currentTimeMillis();
        BigDecimal result = fixture.compute();
        finishTime = System.currentTimeMillis();
        System.out.println(String.format("Time taken: %s",
                Duration.ofMillis(finishTime - startTime)));
        return result;
    }

    private void verifyCompute(int digits) {
        verifyCompute(digits, true);
    }

    private void verifyCompute(int digits, boolean profile) {
        PiCalc fixture = createPiCalc(digits);
        BigDecimal result = profile ? profileCompute(fixture) : fixture.compute();
        BigDecimal expected = PI.setScale(digits, BigDecimal.ROUND_FLOOR);
        assertEquals(expected, result);
    }

    private void verifyComputeRandomDigits(int lower, int upper) {
        verifyComputeRandomDigits(lower, upper, true);
    }

    private void verifyComputeRandomDigits(int lower, int upper, boolean profile) {
        int digits = (int) (Math.random() * (upper - lower + 1)) + lower;
        System.out.println(String.format("Digits to compute: %d", digits));
        verifyCompute(digits, profile);
    }

    private PiCalc createPiCalc(int digits) {
        PiCalc fixture = null;
        try {
            fixture = piCalcFactory.create(digits);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fixture;
    }

}