public class RunTimeTest {
    private static void safe() {
        emptyMethod();
    }
    private static void unsafe(String httpParamCmd) throws Exception {
        eval(httpParamCmd);
    }
    private static void eval(String cmd) throws Exception {
        Runtime.getRuntime().exec(cmd);
    }
    private static void emptyMethod() {
    }
}



