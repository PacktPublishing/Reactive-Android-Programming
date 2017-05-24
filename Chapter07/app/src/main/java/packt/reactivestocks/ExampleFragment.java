package packt.reactivestocks;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import com.trello.rxlifecycle2.components.support.RxFragment;

public class ExampleFragment extends RxFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Observable.interval(1, TimeUnit.SECONDS)
                .compose(this.bindToLifecycle())
                .subscribe();
    }
}
