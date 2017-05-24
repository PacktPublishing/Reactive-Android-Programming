package packt.reactivestocks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class StubComposeActivity extends AppCompatActivity {

    private CompositeDisposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock);

        disposable = new CompositeDisposable();

        Disposable disposable1 = Observable.interval(1, TimeUnit.SECONDS)
                .subscribe();

        Disposable disposable2 = Observable.interval(1, TimeUnit.SECONDS)
                .subscribe();

        Disposable disposable3 = Observable.interval(1, TimeUnit.SECONDS)
                .subscribe();

        disposable.addAll(
                disposable1,
                disposable2,
                disposable3
        );
    }

    @Override
    protected void onDestroy() {
        if (disposable != null) {
            disposable.dispose();
        }
        super.onDestroy();
    }
}
