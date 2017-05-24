package packt.reactivestocks;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public class FileCacheObservableTransformer<R> implements ObservableTransformer<R, R> {
    private final String filename;
    private final Context context;

    FileCacheObservableTransformer(String filename, Context context) {
        this.filename = filename;
        this.context = context;
    }

    public static <R> FileCacheObservableTransformer<R> cacheToLocalFileNamed(String filename, Context context) {
        return new FileCacheObservableTransformer<R>(filename, context);
    }

    @Override
    public ObservableSource<R> apply(Observable<R> upstream) {
        return readFromFile()
                .onExceptionResumeNext(
                        upstream
                                .take(1)
                                .doOnNext(this::saveToFile)
                );
    }

    private void saveToFile(R r) throws IOException {
        ObjectOutputStream objectOutputStream = null;
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(getFilename());
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(r);
        } finally {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
        }
    }

    private Observable<R> readFromFile() {
        return Observable.create(emitter -> {
            ObjectInputStream input = null;
            try {
                final FileInputStream fileInputStream = new FileInputStream(getFilename());
                input = new ObjectInputStream(fileInputStream);
                R foundObject = (R) input.readObject();
                emitter.onNext(foundObject);
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (input != null) {
                    input.close();
                }
                emitter.onComplete();
            }

        });
    }

    private String getFilename() {
        return context.getFilesDir().getAbsolutePath() + File.separator + filename;
    }

}
