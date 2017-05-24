package packt.reactivestocks.storio;

import android.content.Context;

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;

import packt.reactivestocks.StockUpdate;

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

}
