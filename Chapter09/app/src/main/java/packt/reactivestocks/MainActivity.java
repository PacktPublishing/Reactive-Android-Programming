package packt.reactivestocks;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pushtorefresh.storio.sqlite.queries.Query;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.javatuples.Triplet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Callable;
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
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Observable;

public class MainActivity extends RxAppCompatActivity {
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

        log(this.toString());

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

        String query = "select * from yahoo.finance.quote where symbol in ('YHOO','GOOG','MSFT')";
        String env = "store://datatables.org/alltableswithkeys";

        final Configuration configuration = new ConfigurationBuilder()
                .setDebugEnabled(BuildConfig.DEBUG)
                .setOAuthConsumerKey("tTlvwBfqduVadKKEwMXDCmzA4")
                .setOAuthConsumerSecret("FiIOveHm9jLAtf0YSopWROeOFo3OA9VBM2CAuKwZ8AoL1gl4AK")
                .setOAuthAccessToken("195655474-QY8neLxXxqOsF8PGM8MYLsYGyQxQZA73S4qp0Sc2")
                .setOAuthAccessTokenSecret("lIiock0OTkR4TflFPb9pSMjLL8pN9JKIYKBhWMWwtxyMa")
                .build();

        final String[] trackingKeywords = {"Yahoo", "Google", "Microsoft"};
        final FilterQuery filterQuery = new FilterQuery()
                .track(trackingKeywords)
                .language("en");


        Observable.merge(
                Observable.interval(0, 5, TimeUnit.SECONDS)
                        .flatMapSingle(i -> yahooService.yqlQuery(query, env))
                        .map(r -> r.getQuery().getResults().getQuote())
                        .flatMap(Observable::fromIterable)
                        .map(StockUpdate::create)
                        .groupBy(stockUpdate -> stockUpdate.getStockSymbol())
                        .flatMap(groupObservable -> groupObservable.distinctUntilChanged())
                ,
                observeTwitterStream(configuration, filterQuery)
                        .sample(2700, TimeUnit.MILLISECONDS)
                        .map(StockUpdate::create)
                        .filter(stockUpdate -> {
                            for(String keyword : trackingKeywords) {
                                if (stockUpdate.getTwitterStatus().contains(keyword)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .flatMapMaybe(update -> Observable.fromArray(trackingKeywords)
                                .filter(keyword -> update.getTwitterStatus().toLowerCase().contains(keyword.toLowerCase()))
                                .map(keyword -> update)
                                .firstElement()
                        )
        )
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .doOnError(ErrorHandler.get())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(error -> {
                    Toast.makeText(this, "We couldn't reach internet - falling back to local data",
                            Toast.LENGTH_SHORT)
                            .show();
                })
                .observeOn(Schedulers.io())
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
                .doOnNext(update -> log(update))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stockUpdate -> {
                    Log.d("APP", "New update " + stockUpdate.getStockSymbol());
                    noDataAvailableView.setVisibility(View.GONE);
                    stockDataAdapter.add(stockUpdate);
                    recyclerView.smoothScrollToPosition(0);
                }, error -> {
                    if (stockDataAdapter.getItemCount() == 0) {
                        noDataAvailableView.setVisibility(View.VISIBLE);
                    }
                });

//        demo();
//        demo2();
//        demo3();
//        demo5();
//        demo6();
//        demo7();
//        demo8();
//        demo12();
        demo13();
    }

    private void demo13() {
        Observable.just(
                new StockUpdate("APPL", BigDecimal.ONE, new Date(), ""),
                new StockUpdate("APPL", BigDecimal.ONE, new Date(), ""),
                new StockUpdate("GOOG", BigDecimal.ONE, new Date(), ""),
                new StockUpdate("APPL", BigDecimal.ONE, new Date(), "")
        )
                .groupBy(StockUpdate::getStockSymbol)
                .flatMapSingle(groupedObservable -> groupedObservable.count())
                .subscribe(this::log);
    }

    private void demo12() {
        Observable.merge(
                Observable.interval(3, TimeUnit.SECONDS),
                Observable.just(-1L, -2L)
        )
                .subscribe(v -> log("subscribe", v));

    }

    private void demo11() {
        Observable.merge(
                Observable.just(1L, 2L),
                Observable.just(3L, 4L)
        )
                .subscribe(v -> log("subscribe", v));

    }

    private void demo10() {
        Observable.concat(
                Observable.just(-1L, -2L),
                Observable.interval(3, TimeUnit.SECONDS)
        )
                .subscribe(v -> log("subscribe", v));
    }

    private void demo9() {
        Observable.concat(
                Observable.interval(3, TimeUnit.SECONDS),
                Observable.just(-1L, -2L)
        )
                .subscribe(v -> log("subscribe", v));
    }

    private void demo8() {
        Observable.combineLatest(
                Observable.interval(700, TimeUnit.MILLISECONDS),
                Observable.interval(1, TimeUnit.SECONDS),
                (number, interval) -> number + "-" + interval
        )
                .subscribe(e -> log("subscribe", e));

    }

    private void demo6() {
        Observable.zip(
                Observable.just("One", "Two", "Three"),
                Observable.interval(1, TimeUnit.SECONDS),
                (number, interval) -> number + "-" + interval
        )
                .subscribe(e -> log(e));
    }

    private void demo7() {
        Observable.zip(
                Observable.just("One", "Two", "Three")
                        .doOnDispose(() -> log("just", "doOnDispose"))
                        .doOnTerminate(() -> log("just", "doOnTerminate")),
                Observable.interval(1, TimeUnit.SECONDS)
                        .doOnDispose(() -> log("interval", "doOnDispose"))
                        .doOnTerminate(() -> log("interval", "doOnTerminate")),
                (number, interval) -> number + "-" + interval
        )
                .doOnDispose(() -> log("zip", "doOnDispose"))
                .doOnTerminate(() -> log("zip", "doOnTerminate"))
                .subscribe(e -> log(e));
    }

    private void demo5() {
        class User {
            String userId;

            public User(String userId) {
                this.userId = userId;
            }
        }

        class UserCredentials {
            public final User user;
            public final String accessToken;

            public UserCredentials(User user, String accessToken) {
                this.user = user;
                this.accessToken = accessToken;
            }
        }

        Observable.just(new User("1"), new User("2"), new User("3"))
                .map(user -> new UserCredentials(user, "accessToken"))
                .subscribe(credentials -> log(credentials.user.userId, credentials.accessToken));

    }

    private void demo4() {
        Observable.just("UserID1", "UserID2", "UserID3")
                .map(id -> Triplet.with(id, id + "-access-token", "third-value"))
                .subscribe(triplet -> log(triplet.getValue0(), triplet.getValue1() + triplet.getValue2()));
    }

    private void demo3() {
        Observable.just("ID1", "ID2", "ID3")
                .flatMap(id -> Observable.fromCallable(mockHttpRequest(id)))
                .subscribe(value -> log("subscribe-subscribe", value.toString()));
    }

    private void demo2() {
        final Observable<Observable<Date>> map = Observable.just("ID1", "ID2", "ID3")
                .map(id -> Observable.fromCallable(mockHttpRequest(id)));
        map
                .subscribe(e -> {
                    e.subscribe(value -> log("subscribe-subscribe", value.toString()));
                });
    }

    private Callable<Date> mockHttpRequest(String id) {
        return Date::new;
    }

    private void demo() {
        Observable.just(new Date(1), new Date(2), new Date())
                .map(i -> {
                    i.setTime(i.getTime() + 1);
                    return i;
                })
                .subscribe();

        Observable.just(new Date(1), new Date(2), new Date())
                .map(i -> new Date(i.getTime() + 1))
                .subscribe();

        Observable.just(new Date(1), new Date(2), new Date())
                .map(i -> i.toString())
                .subscribe();
    }

    Observable<Status> observeTwitterStream(Configuration configuration, FilterQuery filterQuery) {
        return Observable.create(emitter -> {
            final TwitterStream twitterStream = new TwitterStreamFactory(configuration).getInstance();

            emitter.setCancellable(() -> {
                Schedulers.io().scheduleDirect(() -> twitterStream.cleanUp());
            });

            StatusListener listener = new StatusListener() {
                @Override
                public void onStatus(Status status) {
                    emitter.onNext(status);
                }

                @Override
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                }

                @Override
                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                }

                @Override
                public void onScrubGeo(long userId, long upToStatusId) {
                }

                @Override
                public void onStallWarning(StallWarning warning) {
                }

                @Override
                public void onException(Exception ex) {
                    emitter.onError(ex);
                }
            };

            twitterStream.addListener(listener);
            twitterStream.filter(filterQuery);
        });
    }

    private void saveStockUpdate(StockUpdate stockUpdate) {
        log("saveStockUpdate", stockUpdate.getStockSymbol());
        StorIOFactory.get(this)
                .put()
                .object(stockUpdate)
                .prepare()
                .asRxSingle()
                .toBlocking()
                .value();
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

    private void log(String stage, int item) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    private void log(String stage, long item) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    private void log(String stage) {
        Log.d("APP", stage + ":" + Thread.currentThread().getName());
    }

    private void log(StockUpdate update) {
        Log.d("APP", Thread.currentThread().getName() + ":" + update.toString());
    }

    private void log(long value) {
        Log.d("APP", Thread.currentThread().getName() + ":" + value);
    }
}
