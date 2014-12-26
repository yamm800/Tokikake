package com.example.yamaguchi.tokikake;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.yamaguchi.tokikake.Deferred.Result;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by yamaguchi on 14/12/26.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 重い処理を登録
        Callable<Result<String, String, String>> heavy2 = new Callable<Result<String, String, String>>() {
            @Override public Result<String, String, String> call() throws InterruptedException {
                Thread.sleep(2000);
                Result result = new Result<>("two!", "", "");
                return result;
            }
        };

        // コールバックを登録
        FutureTask<Result<String, String, String>> myTask2 = new FutureTask<Result<String, String, String>>(heavy2) {
            @Override protected void done() {
                try {
                    Log.d("LOG", this.get().mValue);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        final Deferred deferred = new Deferred<Result<String, String, String>>();
        deferred.mExecutor.submit(new Runnable() {
            @Override
            public void run() {
//                deferred.fulfill(new Result<String, String, String>("ok", "", ""));
                deferred.reject(new Result<String, String, String>("", "ng", ""));
            }
        });

        deferred.promise()
                .done(myTask2)
                .fail(mMyTask1);

    }

    FutureTask<Result<String, String, String>> mMyTask1 =
            new FutureTask<Result<String, String, String>>(new Callable<Result<String, String, String>>() {
                @Override public Result<String, String, String> call() throws InterruptedException {
                    Thread.sleep(2000);
                    Result result = new Result<>("one!", "", "");
                    return result;
                }
            }) {
                @Override protected void done() {
                    try {
                        Log.d("LOG", this.get().mValue);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
}
