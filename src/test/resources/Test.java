
public class Test {
    Test2 test, test2;
    Test myTest;
    boolean b;
    int[] a;

    public Test() {
        int a = 0;
        System.out.println(a);
        test = new Test2();
        myTest = new Test();
    }

    public void speak(Test2 testParam){
        System.out.println("Hello World.");
        Test2 testLocal = testParam;
    }

    public static void main() {
        Test test1 = new Test();
        test1.test2 = new Test2();
        test1.speak(new Test2());
    }
}
