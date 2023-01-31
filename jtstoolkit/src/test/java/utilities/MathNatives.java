package utilities;

/**
 * Utility class used to deal with intrinsics in Math library.
 * <p>
 * For your information: StrictMath insures portability by returning the same results on every
 * platform while Math might be optimized by the VM to improve performance.
 * In some edge cases (and if intrinsified), Math results are slightly
 * different.
 * <p>
 * For the record: Math intrinsics can be disabled in JVM by adding the following options:
 * <code>-XX:+UnlockDiagnosticVMOptions -XX:-InlineMathNatives</code>
 *
 * @author Philippe Charles
 * @since 2021/01/08
 */
public final class MathNatives {

    private MathNatives() {
        // static class
    }

    /**
     * Checks if {@link Math#exp(double)} has been intrinsified.
     *
     * @return true if intrinsified, false otherwise
     */
    public static boolean isMathExpIntrinsifiedByVM() {
        double edgeCase = 0.12585918361184556;
        return Math.exp(edgeCase) != StrictMath.exp(edgeCase);
    }

    /**
     * Checks if {@link Math#log(double)} has been intrinsified.
     *
     * @return true if intrinsified, false otherwise
     */
    public static boolean isMathLogIntrinsifiedByVM() {
        double edgeCase = 0.12585918361184556;
        return Math.log(edgeCase) != StrictMath.log(edgeCase);
    }
}
