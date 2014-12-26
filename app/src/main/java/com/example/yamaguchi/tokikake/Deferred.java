package com.example.yamaguchi.tokikake;

import com.example.yamaguchi.tokikake.Deferred.Result;

import java.util.ArrayList;
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

//    public Deferred<RE> notify(RE result) {
//        this.promise().notify(result);
//        return this;
//    }

    public static class Promise<RE extends Result> {

        private enum State {
            Fulfilled,
            Rejected,
            Pending
        }

        RE mResult;

        State mState = State.Pending;

        public boolean fulfilled() { return mState == State.Fulfilled; }
        public boolean rejected() { return mState == State.Rejected; }
        public boolean pending() { return mState == State.Pending; }
        
        private ArrayList<Runnable> mPendingTaskList = new ArrayList<>();
        private ArrayList<Runnable> mProgressTaskList = new ArrayList<>();

        private void fulfill(final RE result) {
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (!pending()) {
                        return;
                    }
                    mResult = result;
                    mState = State.Fulfilled;

                    for (Runnable task : mPendingTaskList) {
                        mExecutor.submit(task);
                    }
                    mPendingTaskList.clear();
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

                    for (Runnable task : mPendingTaskList) {
                        mExecutor.submit(task);
                    }
                    mPendingTaskList.clear();
                }
            });
        }

        private void  notify(final RE result) {
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (!pending()) {
                        return;
                    }

                    for (Runnable task : mProgressTaskList) {
                        mExecutor.submit(task);
                    }
                }
            });
        }

        private void handle(final Runnable task) {
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (pending()) {
                        mPendingTaskList.add(task);
                        return;
                    }
                    mExecutor.submit(task);
                }
            });
        }

        // Done

        public Promise done(final FutureTask<RE> futureTask) {
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

        // Fail

        public Promise fail(final FutureTask<RE> futureTask) {
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
        
        // Progress
        
        public Promise progress(final FutureTask<RE> futureTask) {
            handle(new Runnable() {
                @Override
                public void run() {
                    mExecutor.submit(futureTask);
                }
            });

            return this;
        }

        // Always

        public Promise always(final FutureTask<RE> futureTask) {
            handle(new Runnable() {
                @Override
                public void run() {
                    mExecutor.submit(futureTask);
                }
            });
            return this;
        }
    }
}