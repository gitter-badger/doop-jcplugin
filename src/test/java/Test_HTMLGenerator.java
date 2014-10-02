import org.junit.Test;

import java.io.*;

/**
 * Created by bibou on 2/9/2014.
 */
public class Test_HTMLGenerator extends BaseTest {

    public Test_HTMLGenerator() {
        super();
    }

    @Test
    public void Test_Sum() throws IOException, InterruptedException {
        runCompiler("Test.java");
        runCompiler("Test2.java");
    }
}
