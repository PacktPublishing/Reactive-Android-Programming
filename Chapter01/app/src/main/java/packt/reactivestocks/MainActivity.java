package packt.reactivestocks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.hello_world_salute)
    TextView helloText;

    @BindView(R.id.stock_updates_recycler_view)
    RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private StockDataAdapter stockDataAdapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        demo();

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


}
