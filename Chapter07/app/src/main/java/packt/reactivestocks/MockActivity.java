package packt.reactivestocks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class MockActivity extends AppCompatActivity {

// Memory Leak
// private String[] bigArray = new String[10000000];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock);

        Observable.interval(0, 2, TimeUnit.SECONDS)
                .subscribe(i -> Log.i("APP", "Instance " + this.toString() + " reporting"));

        Log.i("APP", "Activity Created");
    }

//    Memory Leak Example
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_mock);
//
//        bigArray[0] = "test";
//
//        getApplication().registerComponentCallbacks(new ComponentCallbacks() {
//            @Override
//            public void onConfigurationChanged(Configuration newConfig) {
//                Log.i("APP", "Some logging");
//            }
//
//            @Override
//            public void onLowMemory() {
//            }
//        });
//
//        Log.i("APP", "Activity Created");
//    }
}
