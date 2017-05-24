package packt.reactivestocks.storio;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery;

import packt.reactivestocks.StockUpdate;

public class StockUpdatePutResolver extends DefaultPutResolver<StockUpdate> {
    @NonNull
    @Override
    protected InsertQuery mapToInsertQuery(@NonNull StockUpdate object) {
        return InsertQuery.builder()
                .table(StockUpdateTable.TABLE)
                .build();
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull StockUpdate object) {
        return UpdateQuery.builder()
                .table(StockUpdateTable.TABLE)
                .where(StockUpdateTable.Columns.ID + " = ?")
                .whereArgs(object.getId())
                .build();
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull StockUpdate entity) {
        final ContentValues contentValues = new ContentValues();

        contentValues.put(StockUpdateTable.Columns.ID, entity.getId());
        contentValues.put(StockUpdateTable.Columns.STOCK_SYMBOL, entity.getStockSymbol());
        contentValues.put(StockUpdateTable.Columns.PRICE, getPrice(entity));
        contentValues.put(StockUpdateTable.Columns.DATE, getDate(entity));
        contentValues.put(StockUpdateTable.Columns.TWITTER_STATUS, entity.getTwitterStatus());

        return contentValues;
    }

    private long getDate(@NonNull StockUpdate entity) {
        return entity.getDate().getTime();
    }

    private long getPrice(@NonNull StockUpdate entity) {
        return entity.getPrice().scaleByPowerOfTen(4).longValue();
    }
}
