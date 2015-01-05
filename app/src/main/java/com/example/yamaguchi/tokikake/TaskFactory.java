package com.example.yamaguchi.tokikake;

import com.example.yamaguchi.tokikake.Deferred.Result;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by yamaguchi on 15/01/05.
 */
public class TaskFactory<RE extends Result> {

    public TaskFactory() {

    }

    public FutureTask<RE> createTask(final Object obj, final Method m, Callable<RE> callable) {

        return new FutureTask<RE>(callable) {
            @Override
            protected void done() {
                try {
                    m.invoke(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

}
