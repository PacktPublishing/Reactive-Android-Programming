package packt.reactivestocks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import packt.reactivestocks.yahoo.RetrofitYahooServiceFactory;
import packt.reactivestocks.yahoo.YahooService;
import rx.functions.Action1;

import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Flowable;
import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Observable;

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

        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        stockDataAdapter = new StockDataAdapter();
        recyclerView.setAdapter(stockDataAdapter);

        Observable.just("Please use this app responsibly!")
                .subscribe(s -> helloText.setText(s));

        YahooService yahooService = new RetrofitYahooServiceFactory().create();

        String query = "select * from yahoo.finance.quote where symbol in ('YHOO','AAPL','GOOG','MSFT')";
        String env = "store://datatables.org/alltableswithkeys";

        Observable.interval(0, 5, TimeUnit.SECONDS)
                .flatMap(
                        i -> yahooService.yqlQuery(query, env)
                                .toObservable()
                )
                .subscribeOn(Schedulers.io())
                .map(r -> r.getQuery().getResults().getQuote())
                .flatMap(Observable::fromIterable)
                .map(r -> StockUpdate.create(r))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stockUpdate -> {
                    Log.d("APP", "New update " + stockUpdate.getStockSymbol());
                    stockDataAdapter.add(stockUpdate);
                });

        demo();
        demo1();
        demo2();
        demo3();
    }

    private void demo3() {
        toV2Flowable(rx.Observable.just("One", "Two", "Three"))
                .doOnNext(i -> log("doOnNext", i))
                .subscribe(i -> log("subscribe", i));
    }

    private void demo2() {
        v2(rx.Observable.just("One", "Two", "Three"))
                .doOnNext(i -> log("doOnNext", i))
                .subscribe(i -> log("subscribe", i));

    }

    private void demo1() {
        toV2Observable(rx.Observable.just("One", "Two", "Three"))
                .doOnNext(i -> log("doOnNext", i))
                .subscribe(i -> log("subscribe", i));
    }

    public static  <T> Observable<T> v2(rx.Observable<T> source) {
        return toV2Observable(source);
    }


    private void demo() {
        rx.Observable.just(9)
                .onBackpressureBuffer()
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {

                    }
                });

        Observable.just(9)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {

                    }
                });
    }

    private void log(Throwable throwable) {
        Log.e("APP", "Error", throwable);
    }

    private void log(String stage, String item) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    private void log(String stage) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName());
    }

}
