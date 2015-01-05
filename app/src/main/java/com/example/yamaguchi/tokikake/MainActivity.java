package com.example.yamaguchi.tokikake;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.yamaguchi.tokikake.Deferred.Result;
import com.example.yamaguchi.tokikake.TokikakeAnnotation.Tokikake;

/**
 * Created by yamaguchi on 14/12/26.
 */
public class MainActivity extends Activity {
    private static final String CALLABLE_1 = "callable1";
    private static final String CALLABLE_2 = "callable2";
    private static final String CALLBACK_1 = "callback1";
    private static final String CALLBACK_2 = "callback2";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 型引数を与える
        final Deferred deferred = new Deferred<Result<String, String, String>>();
        deferred.mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                deferred.fulfill(new Result<String, String, String>("ok", "", ""));
//                deferred.reject(new Result<String, String, String>("", "ng", ""));
            }
        });

        // Deferredに与える型引数と同じものを与える
        TaskFactory<Result<String, String ,String >> taskFactory = new TaskFactory<>();
        Callbacks<Result<String, String ,String >> callbacks = new Callbacks<>();

        deferred.promise()
                // CALLBACK_1, CALLABLE_1はアノテーションに与えているvalue
                .done(taskFactory.createTask(callbacks, CALLBACK_1, CALLABLE_1))
                .fail(taskFactory.createTask(callbacks, CALLBACK_2, CALLABLE_2));

    }

    /**
     * Tokikakeで処理したいメソッドだけ実装したクラス
     * @param <RE> Deferredに与える型引数と同じものを与える
     */
    private static class Callbacks<RE extends Result<String, String, String>> {
        @Tokikake(CALLABLE_1)
        private Result CallableMethod1() {
            try {
                Thread.currentThread().sleep(1000);
                Log.d("LOG","callableMethod1");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Result result = new Result<>("one!", "", "");
            return result;
        }

        @Tokikake(CALLBACK_1)
        private void TestMethod1(RE result) {
            /**
             * REが返ってくる
             * @see com.example.yamaguchi.tokikake.TaskFactory :: createTask
             */
            Log.d("LOG","testMethod1");
            Log.d("LOG", result.mValue);
        }

        @Tokikake(CALLABLE_2)
        private Result CallableMethod2() {
            try {
                Thread.currentThread().sleep(1000);
                Log.d("LOG","callableMethod2");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Result result = new Result<>("two!", "", "");
            return result;
        }

        @Tokikake(CALLBACK_2 )
        private void TestMethod2(RE result) {
            Log.d("LOG","testMethod2");
        }
    }
}
