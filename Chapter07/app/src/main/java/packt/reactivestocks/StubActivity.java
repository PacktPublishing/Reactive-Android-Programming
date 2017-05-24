package packt.reactivestocks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class StubActivity extends AppCompatActivity {

    private Disposable subscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock);

        subscribe = Observable.interval(1, TimeUnit.SECONDS)
                .subscribe();
    }

    @Override
    protected void onDestroy() {
        if (subscribe != null) {
            subscribe.dispose();
        }
        super.onDestroy();
    }
}
