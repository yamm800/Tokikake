package com.example.yamaguchi.tokikake;

import android.util.Log;

import com.example.yamaguchi.tokikake.Deferred.Result;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by yamaguchi on 14/12/26.
 */
public class Deferred<RE extends Result> {
    
    public static class Result<V, E, P>{
        final V mValue;
        final E mError;
        final P mProgress;
        
        public Result(V value, E error, P progress) {
            mValue = value;
            mError = error;
            mProgress = progress;
        }
    }

    // TODO: executor.shutdown()
    static ExecutorService mExecutor = Executors.newCachedThreadPool();

    private Promise<RE> mPromise = new Promise<RE>();

    public Promise promise() {
        return mPromise;
    }

    public Deferred<RE> fulfill(RE result) {
        this.promise().fulfill(result);
        return this;
    }

    public Deferred<RE> reject(RE result) {
        this.promise().reject(result);
        return this;
    }

    public static class Promise<RE extends Result> {

        Runnable pendingTask;

        private enum State {
            Fulfilled,
            Rejected,
            Pending,
            Cancelled;
        }

        RE mResult;

        State mState = State.Pending;

        public boolean fulfilled() { return mState == State.Fulfilled; }
        public boolean rejected() { return mState == State.Rejected; }
        public boolean pending() { return mState == State.Pending; }

        private void fulfill(final RE result) {
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (!pending()) {
                        return;
                    }
                    mResult = result;
                    mState = State.Fulfilled;

                    if (pendingTask != null) {
                        mExecutor.submit(pendingTask);
                        pendingTask = null;
                    }
                }
            });
            
        }

        private void reject(final RE result) {
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (!pending()) {
                        return;
                    }
                    mResult = result;
                    mState = State.Rejected;

                    if (pendingTask != null) {
                        mExecutor.submit(pendingTask);
                        pendingTask = null;
                    }
                }
            });
        }

        private void handle(final Runnable task) {
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (pending()) {
                        pendingTask = task;
                        return;
                    }
                    mExecutor.submit(task);
                }
            });
        }

        // MARK: Done

        public Promise done(final FutureTask<RE> futureTask) {
            Log.d("LOG", "done");
            handle(new Runnable() {
                @Override
                public void run() {
                    if (fulfilled()) {
                        mExecutor.submit(futureTask);
                    }
                }
            });

            return this;
        }

        // MARK: Fail

        public Promise fail(final FutureTask<RE> futureTask) {
            Log.d("LOG", "fail");
            final Deferred<RE> deferred = new Deferred<RE>();
            handle(new Runnable() {
                @Override
                public void run() {
                    if (rejected()) {
                        mExecutor.submit(futureTask);
                    }
                }
            });
            
            return this;
        }
    }
}