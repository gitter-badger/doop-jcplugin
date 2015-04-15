package src.test.resources.advancedTest.myTests;

/**
 * Created by anantoni on 21/8/2014.
 */
public class Test2 {
    int a;
    boolean b;
    Test2 test2;
    Test test;

    public Test2() {
        int a = 0;
        String s = "Elegxos: " + a;
        int[] testArray;
        System.out.println(s);
        this.test = new Test();
    }

    public Test2(int a, boolean b) {
        this.a = a;
        this.b = b;
    }

    public Test2(int a, boolean b, Test test) {
        this.a = a;
        this.b = b;
        this.test = test;
    }

    public Test2(int a, boolean b, Test test, Test2 test2) {
        this.a = a;
        this.b = b;
        this.test = test;
        this.test2 = test2;
    }

    public void speak() {
        Test2 t2 = new Test2();
        System.out.println("Test2 speakin.");
    }
}
