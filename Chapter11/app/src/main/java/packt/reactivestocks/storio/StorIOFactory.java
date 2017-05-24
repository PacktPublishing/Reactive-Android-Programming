package packt.reactivestocks.storio;

import android.content.Context;

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.Query;

import io.reactivex.Observable;
import packt.reactivestocks.StockUpdate;

import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Observable;

public class StorIOFactory {
    private static StorIOSQLite INSTANCE;

    public synchronized static StorIOSQLite get(Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }

        INSTANCE = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new StorIODbHelper(context))
                .addTypeMapping(StockUpdate.class, SQLiteTypeMapping.<StockUpdate>builder()
                        .putResolver(new StockUpdatePutResolver())
                        .getResolver(new StockUpdateGetResolver())
                        .deleteResolver(new StockUpdateDeleteResolver())
                        .build())
                .build();

        return INSTANCE;
    }


    public static Observable<StockUpdate> createLocalDbStockUpdateRetrievalObservable(Context context) {
        return v2(StorIOFactory.get(context)
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
                .flatMap(Observable::fromIterable);
    }

    private static <T> Observable<T> v2(rx.Observable<T> source) {
        return toV2Observable(source);
    }

}
