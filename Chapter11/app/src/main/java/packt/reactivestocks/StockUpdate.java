package packt.reactivestocks;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import packt.reactivestocks.yahoo.json.YahooStockQuote;
import twitter4j.Status;

public class StockUpdate implements Serializable {
    private final String stockSymbol;
    private final BigDecimal price;
    private final Date date;
    private final String twitterStatus;
    private final BigDecimal movingAverage;
    private Integer id;

    public StockUpdate(String stockSymbol, BigDecimal price, Date date, String twitterStatus) {
        if (stockSymbol == null) {
            stockSymbol = "";
        }

        if (twitterStatus == null) {
            twitterStatus = "";
        }

        if (price == null) {
            price = BigDecimal.ZERO;
        }

        this.stockSymbol = stockSymbol;
        this.price = price;
        this.date = date;
        this.twitterStatus = twitterStatus;
        this.movingAverage = BigDecimal.ZERO;
    }

    public StockUpdate(StockUpdate lastValue, BigDecimal average) {
        this.stockSymbol = lastValue.stockSymbol;
        this.price = lastValue.price;
        this.date = lastValue.date;
        this.twitterStatus = lastValue.twitterStatus;
        this.movingAverage = average;
    }

    public String getTwitterStatus() {
        return twitterStatus;
    }

    public boolean isTwitterStatusUpdate() {
        if (twitterStatus.isEmpty()) {
            return false;
        }

        return true;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Date getDate() {
        return date;
    }

    public static StockUpdate create(YahooStockQuote r) {
        return new StockUpdate(r.getSymbol(), r.getLastTradePriceOnly(), new Date(), "");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static StockUpdate create(Status status) {
        return new StockUpdate("", BigDecimal.ZERO, status.getCreatedAt(), status.getText());
    }

    public BigDecimal getMovingAverage() {
        return movingAverage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StockUpdate that = (StockUpdate) o;

        if (!stockSymbol.equals(that.stockSymbol)) return false;
        if (!price.equals(that.price)) return false;
        if (!twitterStatus.equals(that.twitterStatus)) return false;
        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        int result = stockSymbol.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + twitterStatus.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockUpdate{" +
                "stockSymbol='" + stockSymbol + '\'' +
                ", price=" + price +
                ", movingAverage=" + movingAverage +
                '}';
    }
}
