package packt.reactivestocks;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

public class Sandbox {
    static final CountDownLatch WAIT_LATCH = new CountDownLatch(1);

    public static void main(String... args) throws Exception {

//        demo0();
//        demo1();
//        demo2();
        demo5();


        WAIT_LATCH.await();
    }

    private static void demo5() throws InterruptedException {
        Subject<String> subject = AsyncSubject.create();

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(4)
                .map(Objects::toString)
                .subscribe(subject);

        subject.subscribe(v -> log(v));

        Thread.sleep(5100);


        subject.subscribe(v -> log(v));
    }

    private static void demo4() throws InterruptedException {
        Subject<String> subject = ReplaySubject.create();

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .map(Objects::toString)
                .subscribe(subject);

        Thread.sleep(3100);

        subject.subscribe(v -> log(v));

    }

    private static void demo3() {
        Subject<String> subject = BehaviorSubject.create();

        Observable.interval(0, 2, TimeUnit.SECONDS)
                .map(v -> "A" + v)
                .subscribe(subject);

        subject.subscribe(v -> log(v));

        Observable.interval(1, 1, TimeUnit.SECONDS)
                .map(v -> "B" + v)
                .subscribe(subject);


    }

    private static void demo2() {
        Subject<Long> subject = PublishSubject.create();

        Observable.interval(2, TimeUnit.SECONDS)
                .take(3)
                .doOnComplete(() -> log("Origin-One-doOnComplete"))
                .subscribe(subject);

        Observable.interval(1, TimeUnit.SECONDS)
                .take(2)
                .doOnComplete(() -> log("Origin-Two-doOnComplete"))
                .subscribe(subject);

        subject
                .doOnComplete(() -> log("First-doOnComplete"))
                .subscribe(v -> log(v));
    }

    private static void demo1() throws InterruptedException {
        Subject<Long> subject = PublishSubject.create();

        Observable.interval(2, TimeUnit.SECONDS)
                .take(5)
                .doOnSubscribe((d) -> log("Original-doOnSubscribe"))
                .doOnComplete(() -> log("Original-doOnComplete"))
                .subscribe(subject);

        subject
                .doOnSubscribe((d) -> log("First-doOnSubscribe"))
                .doOnComplete(() -> log("First-doOnComplete"))
                .subscribe(v -> log("First: " + v));

        Thread.sleep(4100);

        subject
                .doOnSubscribe((d) -> log("Second-doOnSubscribe"))
                .doOnComplete(() -> log("Second-doOnComplete"))
                .subscribe(v -> log("Second: " + v));

    }

    private static void demo0() {
        Subject<String> subject = null;

        subject.subscribe(v -> log(v));

        Observable.just("1")
                .subscribe(subject);
    }


    public static void log(String stage, String item) {
        System.out.println(stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    public static void log(String item) {
        System.out.println(Thread.currentThread().getName() + ":" + item);
    }

    private static void log(Long v) {
        log(Objects.toString(v));
    }
}
