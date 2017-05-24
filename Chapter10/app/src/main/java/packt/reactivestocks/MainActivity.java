package packt.reactivestocks;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
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
import static packt.reactivestocks.FileCacheObservableTransformer.cacheToLocalFileNamed;
import static packt.reactivestocks.TimingObservableTransformer.timeItems;
import static packt.reactivestocks.storio.LocalItemPersistenceHandlingTransformer.addLocalItemPersistenceHandling;

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
                createFinancialStockUpdateObservable(yahooService, query, env),
                createTweetStockUpdateObservable(configuration, trackingKeywords, filterQuery)
        )
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .doOnError(ErrorHandler.get())
                .compose(addUiErrorHandling())
                .compose(addLocalItemPersistenceHandling(this))
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

//        cachingExample();
        timingExample2();

    }

    private void timingExample2() {
        final Observable<Long> observable = Observable.interval(4, TimeUnit.SECONDS)
                .compose(timeItems((seconds) -> {
                    Log.d("APP", "Seconds passed since the start: " + seconds);
                }));

        observable.subscribe(this::log);
        observable.subscribe(this::log);

    }

    private void timingExample() {
        Observable.interval(4, TimeUnit.SECONDS)
                .compose(timeItems((seconds) -> {
                    Log.d("APP", "Seconds passed since the start: " + seconds);
                }))
                .subscribe(this::log);

    }

    private void cachingExample() {
        Observable.just("1")
                .compose(cacheToLocalFileNamed("test", this))
                .subscribe(this::log);
    }
//    @NonNull
//    private ObservableTransformer<StockUpdate, StockUpdate> addLocalItemPersistenceHandling() {
//        return upstream -> upstream.doOnNext(this::saveStockUpdate)
//                .onExceptionResumeNext(StorIOFactory.createLocalDbStockUpdateRetrievalObservable(this));
//
//    }

    @NonNull
    private ObservableTransformer<StockUpdate, StockUpdate> addUiErrorHandling() {
        return upstream -> upstream.observeOn(AndroidSchedulers.mainThread())
                .doOnError(MainActivity.this::showToastErrorNotificationMethod)
                .observeOn(Schedulers.io());
    }

//    private Observable<StockUpdate> addLocalItemPersistenceHandling(Observable<StockUpdate> observable) {
//        return observable.doOnNext(this::saveStockUpdate)
//                .onExceptionResumeNext(StorIOFactory.createLocalDbStockUpdateRetrievalObservable(this));
//    }

    private Observable<StockUpdate> addUiErrorHandling(Observable<StockUpdate> observable) {
        return observable.observeOn(AndroidSchedulers.mainThread())
                .doOnError(this::showToastErrorNotificationMethod)
                .observeOn(Schedulers.io());
    }

    private Observable<StockUpdate> createTweetStockUpdateObservable(Configuration configuration, String[] trackingKeywords, FilterQuery filterQuery) {
        return observeTwitterStream(configuration, filterQuery)
                .sample(2700, TimeUnit.MILLISECONDS)
                .map(StockUpdate::create)
                .filter(containsAnyOfKeywords(trackingKeywords))
                .flatMapMaybe(skipTweetsThatDoNotContainKeywords(trackingKeywords));
    }

    private Observable<StockUpdate> createFinancialStockUpdateObservable(YahooService yahooService, String query, String env) {
        return Observable.interval(0, 5, TimeUnit.SECONDS)
                .flatMapSingle(i -> yahooService.yqlQuery(query, env))
                .map(r -> r.getQuery().getResults().getQuote())
                .flatMap(Observable::fromIterable)
                .map(StockUpdate::create)
                .groupBy(stockUpdate -> stockUpdate.getStockSymbol())
                .flatMap(groupObservable -> groupObservable.distinctUntilChanged());
    }

    @NonNull
    private Function<StockUpdate, MaybeSource<? extends StockUpdate>> skipTweetsThatDoNotContainKeywords(String[] trackingKeywords) {
        return update -> Observable.fromArray(trackingKeywords)
                .filter(keyword -> update.getTwitterStatus().toLowerCase().contains(keyword.toLowerCase()))
                .map(keyword -> update)
                .firstElement();
    }

    private void showToastErrorNotificationMethod(Throwable error) {
        Toast.makeText(this, "We couldn't reach internet - falling back to local data",
                Toast.LENGTH_SHORT)
                .show();
    }

    @NonNull
    private Consumer<Throwable> showToastErrorNotification() {
        return error -> {
            Toast.makeText(this, "We couldn't reach internet - falling back to local data",
                    Toast.LENGTH_SHORT)
                    .show();
        };
    }

    @NonNull
    private Predicate<StockUpdate> containsAnyOfKeywords(String[] trackingKeywords) {
        return stockUpdate -> {
            for (String keyword : trackingKeywords) {
                if (stockUpdate.getTwitterStatus().contains(keyword)) {
                    return true;
                }
            }
            return false;
        };
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
