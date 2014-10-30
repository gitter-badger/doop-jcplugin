package src.test.resources.advancedTest.myTests;

public class Test {
    Test2 test, test2;
    Test myTest;
    boolean b;
    int[] a;

    class NestedTest {
        Test nestedMyTest;
        Test2 nestedTest, nestedTest2;

        public NestedTest(Test2 a, Test2 b, Test c) {
            nestedMyTest = c;
            nestedTest = a;
            nestedTest2 = b;
        }

        public Test2 speak() {
            Test uniqueLocal = new Test();
            uniqueLocal.speak();
            Test2 testLocal = new Test2();
            testLocal.speak();
            return testLocal;
        }

        public Test speak(Test testParam) {
            Test testLocal = testParam;
            return testLocal;
        }

        class NestedNestedTest {
            Test test;
            Test2 test2, myTest2;

            public NestedNestedTest() {
                Test localTest = new Test();
                test = localTest;
            }

            public void speak() {
                Test speakTest = new Test();
            }

            public void speak(NestedNestedTest nnTest, NestedTest nTest) {
                Test2 a = new Test2();
                Test2 b = new Test2();
                Test c = new Test();
                NestedTest nestedTest = new NestedTest(a, b, c);
                NestedTest d = nTest;
                NestedNestedTest e = nnTest;
            }
        }
    }

    public Test() {
        int a = 0;
        System.out.println(a);
        test = new Test2();
        myTest = new Test();
    }

    public Test(Test myTest, Test2 test, Test2 test2) {
        this.myTest = myTest;
        this.test = test;
        this.test2 = test2;
    }

    public void speak(Test2 testParam, boolean testParam1, Test[] testParam2) {
        System.out.println("Hello world.");
        Test2 testLocal = testParam;
        boolean testLocal1 = testParam1;
        Test[] testLocal2 = testParam2;
    }

    public void speak(Test2 testParam){
        System.out.println("Hello World.");
        Test2 testLocal = testParam;
    }

    public void speak() {
        System.out.println("Hello World.");
        Test2 testLocal = new Test2();
    }

    public static void main(String[] args) {
        Test test1 = new Test();
        test1.test2 = new Test2();
        test1.speak(new Test2());
    }
}
