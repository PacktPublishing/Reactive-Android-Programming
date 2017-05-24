package packt.reactivestocks.storio;

import android.content.Context;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import packt.reactivestocks.StockUpdate;

public class LocalItemPersistenceHandlingTransformer implements ObservableTransformer<StockUpdate, StockUpdate> {
    private final Context context;

    private LocalItemPersistenceHandlingTransformer(Context context) {
        this.context = context;
    }

    public static LocalItemPersistenceHandlingTransformer addLocalItemPersistenceHandling(Context context) {
        return new LocalItemPersistenceHandlingTransformer(context);
    }

    @Override
    public ObservableSource<StockUpdate> apply(Observable<StockUpdate> upstream) {
        return upstream.doOnNext(this::saveStockUpdate)
                .onExceptionResumeNext(StorIOFactory.createLocalDbStockUpdateRetrievalObservable(context));
    }

    private void saveStockUpdate(StockUpdate stockUpdate) {
        StorIOFactory.get(context)
                .put()
                .object(stockUpdate)
                .prepare()
                .asRxSingle()
                .toBlocking()
                .value();
    }
}
