package packt.reactivestocks;


import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.trello.rxlifecycle2.android.RxLifecycleAndroid;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

public class ExampleLifecycleActivity extends RxAppCompatActivity {

    @BindView(R.id.example_hello_id)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock);
        ButterKnife.bind(this);

        Observable.interval(1, TimeUnit.SECONDS)
                .doOnDispose(() -> Log.i("APP", "Disposed"))
                .compose(bindToLifecycle())
                .subscribe();

        Observable.interval(1, TimeUnit.SECONDS)
                .compose(RxLifecycleAndroid.bindActivity(lifecycle()))
                .subscribe();

        Observable.interval(1, TimeUnit.SECONDS)
                .compose(RxLifecycleAndroid.bindView(textView))
                .subscribe();
    }
}
