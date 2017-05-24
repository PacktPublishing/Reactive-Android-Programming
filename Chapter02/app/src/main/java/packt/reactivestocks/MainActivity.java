package packt.reactivestocks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.hello_world_salute)
    TextView helloText;

    @BindView(R.id.stock_updates_recycler_view)
    RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StockDataAdapter stockDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

//        demo();
//        demo1();
//        demo2();
//        demo3();
//        demo4();
        demo5();
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        stockDataAdapter = new StockDataAdapter();
        recyclerView.setAdapter(stockDataAdapter);

        Observable.just("Please use this app responsibly!")
                .subscribe(s -> helloText.setText(s));

        Observable.just(
                new StockUpdate("GOOGLE", 12.43, new Date()),
                new StockUpdate("APPL", 645.1, new Date()),
                new StockUpdate("TWTR", 1.43, new Date())
        )
                .subscribe(stockUpdate -> {
                    Log.d("APP", "New update " + stockUpdate.getStockSymbol());
                    stockDataAdapter.add(stockUpdate);
                });
    }

    private void demo5() {
        Completable completable = Completable.fromAction(() -> {
            log("Let's do something");
        });

        completable.subscribe(() -> {
            log("Finished");
        }, throwable -> {
            log(throwable);
        });


        Single.just("One item")
                .subscribe((item) -> {
                    log(item);
                }, (throwable) -> {
                    log(throwable);
                });

        Maybe.empty();
        Maybe.just("Item")
                .subscribe(s -> {
                    log("On Success: " + s);
                });

        Maybe.just("Item")
                .subscribe(s -> {
                    log("On Success: " + s);
                }, throwable -> log("error"));

        Maybe.just("Item")
                .subscribe(
                        s -> log("success: " + s),
                        throwable -> log("error"),
                        () -> log("onComplete")
                );
    }

    private void demo4() {
        PublishSubject<Integer> observable = PublishSubject.create();

        observable.toFlowable(BackpressureStrategy.MISSING)
                .buffer(10)
                .observeOn(Schedulers.computation())
                .subscribe(v -> log("s", v.toString()), this::log);

        for (int i = 0; i < 1000000; i++) {
            observable.onNext(i);
        }
    }

    private void log(Throwable throwable) {
        Log.e("APP", "Error", throwable);
    }

    private void demo3() {
        Observable.just("One", "Two").toFlowable(BackpressureStrategy.DROP).onBackpressureDrop().buffer(10);

    }

    private void demo2() {
        Observable.just("One", "Two")
                .doOnDispose(() -> log("doOnDispose"))
                .doOnComplete(() -> log("doOnComplete"))
                .doOnNext(e -> log("doOnNext", e))
                .doOnEach(e -> log("doOnEach"))
                .doOnSubscribe((e) -> log("doOnSubscribe"))
                .doOnTerminate(() -> log("doOnTerminate"))
                .doFinally(() -> log("doFinally"))
                .subscribe(e -> log("subscribe", e));
    }

    private void log(String stage, String item) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    private void log(String stage) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName());
    }

    public void demo1() {
//        Observable.just("First item", "Second item")
//                .subscribeOn(Schedulers.io())
//                .doOnNext(e -> Log.d("APP", "on-next:" + Thread.currentThread().getName() + ":" + e))
//                .subscribe(e -> Log.d("APP", "subscribe:" + Thread.currentThread().getName() + ":" + e));

        Observable.just("First item", "Second item")
                .doOnNext(e -> Log.d("APP", "on-next:" + Thread.currentThread().getName() + ":" + e))
                .subscribeOn(Schedulers.computation())
                .subscribe(e -> Log.d("APP", "subscribe:" + Thread.currentThread().getName() + ":" + e));
    }

    public void demo() {

        Observable.just("1")
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String e) {
                        Log.d("APP", "Hello " + e);
                    }
                });

        Observable.just("1")
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return s + "mapped";
                    }
                })
                .flatMap(new Function<String, Observable<String>>() {
                    @Override
                    public Observable<String> apply(String s) {
                        return Observable.just("flat-" + s);
                    }
                })
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Log.d("APP", "on next " + s);
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String e) {
                        Log.d("APP", "Hello " + e);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.d("APP", "Error!");
                    }
                });

        Observable.just("1")
                .map(s -> s + "mapped")
                .flatMap(s -> Observable.just("flat-" + s))
                .doOnNext(s -> Log.d("APP", "on next " + s))
                .subscribe(e -> Log.d("APP", "Hello " + e),
                        throwable -> Log.d("APP", "Error!"));

        Observable.just("1")
                .subscribe(e -> Log.d("APP", "Hello " + e));
    }

}
