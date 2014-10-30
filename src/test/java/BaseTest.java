import javax.tools.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

/**
 * Created by bibou on 3/9/2014.
 */
public class BaseTest {
    public static final String XPLUGIN_STREAM_OPTIMIZER_PLUGIN = "-Xplugin:DoopPrinterPlugin";

    public static final String SRC_TEST_RESOURCES = "src/test/resources/advancedTest/myTests/";

    protected PrintWriter out;
    protected PrintWriter err;

    public BaseTest() {
        out = new PrintWriter(System.out, true);
        err = new PrintWriter(System.err, true);
    }

    protected void runCompiler(String pathname) throws IOException {
        String[] compileOptions = new String[]{ XPLUGIN_STREAM_OPTIMIZER_PLUGIN };

        pathname = SRC_TEST_RESOURCES + pathname;

        Iterable<String> compilationOptions = asList(compileOptions);

        JavaCompiler compiler = getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(new File(pathname));

        Boolean result = compiler.getTask(null, fileManager, diagnostics, compilationOptions, null, compilationUnits).call();

        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {

            System.out.println(diagnostic.getCode());
            System.out.println(diagnostic.getKind());
            System.out.println(diagnostic.getPosition());
            System.out.println(diagnostic.getStartPosition());
            System.out.println(diagnostic.getEndPosition());
            System.out.println(diagnostic.getSource());
            System.out.println(diagnostic.getMessage(null));

        }
        System.out.println("Result: " + result);

        fileManager.close();
    }

    // Inspired by JTREG (http://hg.openjdk.java.net/code-tools/jtreg/file/tip/src/share/classes/com/sun/javatest/regtest/Main.java)
    protected String runJVM(String test) throws IOException, InterruptedException {
        Runtime r = Runtime.getRuntime();
        Process p = null;

        try {

            List<String> listArgs = new ArrayList<String>();

            String jdk = discoverJDK();

            listArgs.add(jdk);
            listArgs.add("-cp");
            listArgs.add(SRC_TEST_RESOURCES);
            listArgs.add(test);

            String[] res = new String[listArgs.size()];

            // Execute java runtime.
            p = r.exec(listArgs.toArray(res));

            // Hold the output in a string.
            StringWriter sw = new StringWriter();
            PrintWriter accumulator = new PrintWriter(sw);

            InputStream childOut = p.getInputStream();
            StreamCopier childOutCopier = new StreamCopier(childOut, accumulator);
            childOutCopier.start();

            InputStream childErr = p.getErrorStream();
            StreamCopier childErrCopier = new StreamCopier(childErr, err);
            childErrCopier.start();

            OutputStream childIn = p.getOutputStream();
            if (childIn != null)
                childIn.close();

            childOutCopier.waitUntilDone();
            childErrCopier.waitUntilDone();

            int exitCode = p.waitFor();

            p = null;

            childOut.close();
            childErr.close();

            return sw.toString().trim();
        }
        catch (IOException|InterruptedException e) {
            throw e;
        } finally {
            if (p != null)
                p.destroy();
        }
    }

    private String discoverJDK() {
        String s = System.getenv("JAVA_HOME");

        if (s == null || s.length() == 0)
            s = System.getProperty("java.home");

        s += "/bin/java";
        return s;
    }

    // Inspired by JTREG (http://hg.openjdk.java.net/code-tools/jtreg/file/tip/src/share/classes/com/sun/javatest/regtest/Main.java)
    static class StreamCopier extends Thread {
        /**
         * Create one.
         * @param from  the stream to copy from
         * @param to    the stream to copy to
         */
        StreamCopier(InputStream from, PrintWriter to) {
            super(Thread.currentThread().getName() + "_StreamCopier_" + (serial++));
            in = new BufferedReader(new InputStreamReader(from));
            out = to;
        }

        /**
         * Set the thread going.
         */
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            //System.out.println("Copying stream");
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    out.println(line);
                }
            } catch (IOException e) {
            }
            synchronized (this) {
                done = true;
                notifyAll();
            }
        }

        public synchronized boolean isDone() {
            return done;
        }

        /**
         * Blocks until the copy is complete, or until the thread is interrupted
         */
        public synchronized void waitUntilDone() throws InterruptedException {
            boolean interrupted = false;

            // poll interrupted flag, while waiting for copy to complete
            while (!(interrupted = Thread.interrupted()) && !done)
                wait(1000);

            if (interrupted)
                throw new InterruptedException();
        }

        private BufferedReader in;
        private PrintWriter out;
        private boolean done;
        private static int serial;

    }
}
