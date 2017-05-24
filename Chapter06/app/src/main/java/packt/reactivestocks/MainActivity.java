package packt.reactivestocks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import packt.reactivestocks.storio.StockUpdateTable;
import packt.reactivestocks.storio.StorIOFactory;
import packt.reactivestocks.yahoo.RetrofitYahooServiceFactory;
import packt.reactivestocks.yahoo.YahooService;
import packt.reactivestocks.yahoo.json.YahooStockResult;

import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Observable;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.hello_world_salute)
    TextView helloText;

    @BindView(R.id.no_data_available)
    TextView noDataAvailableView;

    @BindView(R.id.stock_updates_recycler_view)
    RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StockDataAdapter stockDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxJavaPlugins.setErrorHandler(ErrorHandler.get());

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
                        i -> Observable.<YahooStockResult>error(new RuntimeException("Crash"))
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(error -> {
                    log("doOnError", "error");
                    Toast.makeText(this, "We couldn't reach internet - falling back to local data",
                            Toast.LENGTH_SHORT)
                            .show();
                })
                .observeOn(Schedulers.io())
                .map(r -> r.getQuery().getResults().getQuote())
                .flatMap(Observable::fromIterable)
                .map(StockUpdate::create)
                .doOnNext(this::saveStockUpdate)
                .onExceptionResumeNext(
                        v2(StorIOFactory.get(this)
                                .get()
                                .listOfObjects(StockUpdate.class)
                                .withQuery(Query.builder()
                                        .table(StockUpdateTable.TABLE)
                                        .orderBy("date DESC")
                                        .limit(50)
                                        .build())
                                .prepare()
                                .asRxObservable())
                                .take(1)
                                .flatMap(Observable::fromIterable)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stockUpdate -> {
                    Log.d("APP", "New update " + stockUpdate.getStockSymbol());
                    noDataAvailableView.setVisibility(View.GONE);
                    stockDataAdapter.add(stockUpdate);
                }, error -> {
                    if (stockDataAdapter.getItemCount() == 0) {
                        noDataAvailableView.setVisibility(View.VISIBLE);
                    }
                });

//        demo10();
    }

    private void demo10() {
        StockUpdate stockUpdate = null;
        StorIOFactory.get(this)
                .delete()
                .object(stockUpdate)
                .prepare()
                .asRxCompletable()
                .subscribe();
    }

    private void demo9() {
        Observable.<String>error(new Error("Crash!"))
                .subscribe(item -> {
                    log("subscribe", item);
                }, (throwable) -> {
                });

    }

    private void demo8() {
        Observable.<String>error(new Error("Crash!"))
                .doOnError(ErrorHandler.get())
                .subscribe(item -> {
                    log("subscribe", item);
                }, ErrorHandler.get());

    }

    private void demo7() {
        Observable.<String>error(new Error("Crash!"))
                .onErrorReturnItem("ReturnItem")
                .subscribe(item -> {
                    log("subscribe", item);
                }, e -> log("subscribe", e));

    }

    private void demo6() {
        Observable.<String>error(new Error("Crash!"))
                .onErrorReturn(throwable -> "Return")
                .subscribe(item -> {
                    log("subscribe", item);
                }, e -> log("subscribe", e));
    }

    private void demo5() {
        Observable.<String>error(new RuntimeException("Crash!"))
                .doOnError(e -> log("doOnNext", e))
                .onExceptionResumeNext(Observable.just("Second"))
                .subscribe(item -> {
                    log("subscribe", item);
                }, e -> log("subscribe", e));
    }

    private void demo4() {
        Observable.<String>error(new RuntimeException("Crash!"))
                .onExceptionResumeNext(Observable.just("Second"))
                .subscribe(item -> {
                    log("subscribe", item);
                }, e -> log("subscribe", e));
    }

    private void demo3() {
        Observable.just("One")
                .doOnNext(i -> {
                    throw new RuntimeException("Crash!");
                })
                .doOnError(e -> log("doOnError", e))
                .subscribe(item -> {
                    log("subscribe", item);
                }, e -> log("subscribe", e));
    }

    private void demo2() {
        Observable.just("One")
                .doOnNext(i -> {
                    throw new RuntimeException("Very wrong");
                })
                .subscribe(item -> log("subscribe", item), this::log);
    }

    private void demo1() {
        Observable.just("One")
                .doOnNext(i -> {
                    throw new RuntimeException();
                })
                .subscribe(item -> {
                    log("subscribe", item);
                });
    }

    private void saveStockUpdate(StockUpdate stockUpdate) {
        log("saveStockUpdate", stockUpdate.getStockSymbol());
        final PutResult value = StorIOFactory.get(this)
                .put()
                .object(stockUpdate)
                .prepare()
                .asRxSingle()
                .toBlocking()
                .value();

        final Long aLong = value.insertedId();
    }

    private void demo0() {
        StorIOFactory.get(this)
                .put()
                .object(new StockUpdate("GOOG", new BigDecimal(10.0), new Date()))
                .prepare()
                .asRxSingle()
                .subscribe();
    }

    public static <T> Observable<T> v2(rx.Observable<T> source) {
        return toV2Observable(source);
    }

    private void log(Throwable throwable) {
        Log.e("APP", "Error on " + Thread.currentThread().getName() + ":", throwable);
    }

    private void log(String stage, Throwable throwable) {
        Log.e("APP", stage + ":" + Thread.currentThread().getName() + ": error", throwable);
    }

    private void log(String stage, String item) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    private void log(String stage) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName());
    }

}
