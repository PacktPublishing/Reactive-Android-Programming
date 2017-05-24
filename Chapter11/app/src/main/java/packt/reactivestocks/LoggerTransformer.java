package packt.reactivestocks;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public class LoggerTransformer<R> implements ObservableTransformer<R, R> {

    private final String tag;

    public LoggerTransformer(String tag) {
        this.tag = tag;
    }

    public static <R> LoggerTransformer<R> debugLog(String tag) {
        return new LoggerTransformer<>(tag);
    }

    @Override
    public ObservableSource<R> apply(Observable<R> upstream) {
        return upstream
                .doOnNext(v -> this.log("doOnNext", v))
                .doOnError(error -> this.log("doOnError", error))
                .doOnComplete(() -> this.log("doOnComplete", upstream.toString()))
                .doOnTerminate(() -> this.log("doOnTerminate", upstream.toString()))
                .doOnDispose(() -> this.log("doOnDispose", upstream.toString()))
                .doOnSubscribe(v -> this.log("doOnSubscribe", upstream.toString()));
    }

    private void log(String stage, Object item) {
        Log.d("APP-DEBUG:" + tag, stage + ":" + Thread.currentThread().getName() + ":" + item);
    }

    private void log(String stage, Throwable throwable) {
        Log.e("APP-DEBUG:" + tag, stage + ":" + Thread.currentThread().getName() + ": error", throwable);
    }
}
