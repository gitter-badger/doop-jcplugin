public class Test {
    Test2 test;
    Test myTest;
    boolean b;
    int[] a;

    public Test() {
        int a = 0;
        System.out.println(a);
	test = new Test2();
	myTest = new Test();
    }

    public void Speak(){
        System.out.println("Hello World.");
    }
}
