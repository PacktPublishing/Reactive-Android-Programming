package packt.reactivestocks;

import java.util.concurrent.CountDownLatch;

public class Sandbox {
    static final CountDownLatch WAIT_LATCH = new CountDownLatch(1);

    public static void main(String... args) throws Exception {

//        demo1();

        WAIT_LATCH.await();
    }


    public static void log(String stage, String item) {
        System.out.println(stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    public static void log(String item) {
        System.out.println(Thread.currentThread().getName() + ":" + item);
    }
}
