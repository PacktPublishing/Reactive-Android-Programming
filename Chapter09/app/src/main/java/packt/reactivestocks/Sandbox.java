package packt.reactivestocks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class Sandbox {
    static final CountDownLatch WAIT_LATCH = new CountDownLatch(1);

    public static void main(String... args) throws Exception {
        demo0();
//        demo1();

        WAIT_LATCH.await();
    }

    private static void demo1() {
        Observable.interval(3, TimeUnit.SECONDS)
                .take(2)
                .flatMap(x -> Observable.interval(1, TimeUnit.SECONDS)
                        .map(i -> x + "A" + i)
                        .take(5)
                )
                .subscribe(item -> log("flatMap", item));
    }

    private static void demo0() {
        Observable.interval(3, TimeUnit.SECONDS)
                .take(2)
                .switchMap(x -> Observable.interval(1, TimeUnit.SECONDS)
                        .map(i -> x + "A" + i)
                        .take(5)
                )
                .subscribe(item -> log("switchMap", item));
    }


    public static void log(String stage, String item) {
        System.out.println(stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    public static void log(String item) {
        System.out.println(Thread.currentThread().getName() + ":" + item);
    }
}
