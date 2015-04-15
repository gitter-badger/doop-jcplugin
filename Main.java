import extras.*;

public class Main {

    public static void main(String[] args) {
        Test test1 = new Test();
        test1.test2 = new Test2();
        test1.speak(new Test2());
    }
    
}
