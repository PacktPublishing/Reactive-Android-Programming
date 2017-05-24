package packt.reactivestocks.yahoo;

import io.reactivex.Single;
import packt.reactivestocks.yahoo.json.YahooStockResult;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YahooService {

    @GET("yql?format=json")
    Single<YahooStockResult> yqlQuery(
            @Query("q") String query,
            @Query("env") String env
    );
}
