package packt.reactivestocks;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class Sandbox {
    static final CountDownLatch WAIT_LATCH = new CountDownLatch(1);

    public static void main(String... args) throws Exception {
        demo9();
    }

    private static void demo9() throws Exception {

        Observable.range(1, 100)
                .doOnNext(i -> log("doOnNext", i.toString()))
                .flatMap(i -> Observable.just(i)
                        .subscribeOn(Schedulers.io())
                        .map(Sandbox::importantLongTask)
                )
                .doOnTerminate(WAIT_LATCH::countDown)
                .map(Object::toString)
                .subscribe(e -> log("subscribe", e));

        WAIT_LATCH.await();
    }

    private static void demo8() throws InterruptedException {
        Observable.range(1, 1000)
                .map(Object::toString)
                .doOnNext(i -> log("doOnNext", i))
                .observeOn(Schedulers.computation())
                .subscribe(i -> log("subscribe", i));

        WAIT_LATCH.await();
    }

    private static void demo7() throws InterruptedException {
        Observable.just("One", "Two")
                .doOnNext(i -> log("doOnNext", i))
                .observeOn(Schedulers.computation())
                .subscribe(i -> log("subscribe", i));

        WAIT_LATCH.await();
    }

    private static void demo6() {
        Observable.just("One", "Two")
                .doOnNext(i -> log("doOnNext", i))
                .subscribe(i -> log("subscribe", i));

        log("doOnNext", "One");
        log("subscribe", "One");
        log("doOnNext", "Two");
        log("subscribe", "Two");
    }

    private static void demo5() throws InterruptedException {

        Observable.just("One", "Two", "Three")
                .doOnNext(i -> log("doOnNext", i))
                .observeOn(Schedulers.newThread())
                .doOnNext(i -> log("doOnNext", i))
                .observeOn(Schedulers.computation())
                .subscribe(i -> log("subscribe", i));

        WAIT_LATCH.await();
    }

    private static void demo4() throws InterruptedException {
        Observable.just("One", "Two", "Three")
                .subscribeOn(Schedulers.single())
                .doOnNext(i -> log("doOnNext", i))
                .subscribeOn(Schedulers.newThread())
                .doOnNext(i -> log("doOnNext", i))
                .subscribeOn(Schedulers.io())
                .subscribe(i -> log("subscribe", i));

        WAIT_LATCH.await();
    }

    private void demo0() {
        Schedulers.single();
        Schedulers.trampoline();
        Schedulers.newThread();
        Schedulers.computation();
        Schedulers.io();
        Schedulers.io();
    }

    private static void demo3() {
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final Scheduler pooledScheduler = Schedulers.from(executor);

        Observable.range(1, 100)
                .subscribeOn(pooledScheduler)
                .map(Object::toString)
                .subscribe(e -> log("subscribe", e));
    }

    private static void demo2() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(1000);
        final Scheduler pooledScheduler = Schedulers.from(executor);

        Observable.range(1, 10000)
                .flatMap(i -> Observable.just(i)
                        .subscribeOn(pooledScheduler)
                        .map(Sandbox::importantLongTask)
                )
                .doOnTerminate(WAIT_LATCH::countDown)
                .map(Object::toString)
                .subscribe(e -> log("subscribe", e));

        WAIT_LATCH.await();
        executor.shutdown();
    }

    private static void demo1() throws Exception {
        Observable.range(1, 10000)
                .flatMap(i -> Observable.just(i)
                        .subscribeOn(Schedulers.io())
                        .map(Sandbox::importantLongTask)
                )
                .doOnTerminate(WAIT_LATCH::countDown)
                .map(Object::toString)
                .subscribe(e -> log("subscribe", e));

        WAIT_LATCH.await();

    }

    public static int importantLongTask(int i) {
        try {
            long minMillis = 10L;
            long maxMillis = 1000L;
            log("Working on " + i);
            final long waitingTime = (long) (minMillis + (Math.random() * maxMillis - minMillis));
            Thread.sleep(waitingTime);
            return i;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void demo() {
        Observable.just("One", "Two")
                .observeOn(Schedulers.single())
                .subscribe(e -> log("subscribe", e));

        Observable.just("1", "2", "3", "4", "5")
                .subscribeOn(Schedulers.trampoline())
                .retry()
                .doOnNext(i -> log("doOnNext", i))
                .subscribe(Sandbox::log);

        Observable.just("a", "b", "c", "d", "e")
                .subscribe(Sandbox::log);
    }

    public static void log(String stage, String item) {
        System.out.println(stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    public static void log(String item) {
        System.out.println(Thread.currentThread().getName() + ":" + item);
    }
}
