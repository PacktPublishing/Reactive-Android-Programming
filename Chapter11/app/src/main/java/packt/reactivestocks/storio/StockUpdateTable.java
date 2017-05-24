package packt.reactivestocks.storio;

public class StockUpdateTable {
    public static final String TABLE = "stock_updates";

    static class Columns {
        static final String ID = "_id";
        static final String STOCK_SYMBOL = "stock_symbol";
        static final String PRICE = "price";
        static final String DATE = "date";
        static final String TWITTER_STATUS = "twitter_status";
    }

    private StockUpdateTable() {
    }

    static String createTableQuery() {
        return "CREATE TABLE " + TABLE + "("
                + Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Columns.STOCK_SYMBOL + " TEXT NOT NULL, "
                + Columns.DATE + " LONG NOT NULL, "
                + Columns.PRICE + " LONG NOT NULL, "
                + Columns.TWITTER_STATUS + " TEXT NULL "
                + ");";
    }
}