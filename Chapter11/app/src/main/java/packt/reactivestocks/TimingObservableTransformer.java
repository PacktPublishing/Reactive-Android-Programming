package packt.reactivestocks;

import android.util.Pair;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;

public class TimingObservableTransformer<R> implements ObservableTransformer<R, R> {

    private final Consumer<Long> timerAction;

    public TimingObservableTransformer(Consumer<Long> timerAction) {
        this.timerAction = timerAction;
    }

    public static <R> TimingObservableTransformer<R> timeItems(Consumer<Long> timerAction) {
        return new TimingObservableTransformer<>(timerAction);
    }

    @Override
    public ObservableSource<R> apply(Observable<R> upstream) {
        return Observable.combineLatest(
                Observable.just(new Date()),
                upstream,
                Pair::create
        )
                .doOnNext((pair) -> {
                    Date currentTime = new Date();
                    long diff = currentTime.getTime() - pair.first.getTime();
                    long diffSeconds = diff / 1000;

                    timerAction.accept(diffSeconds);
                })
                .map(pair -> pair.second);
    }

}
