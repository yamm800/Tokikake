package com.example.yamaguchi.tokikake;

import com.example.yamaguchi.tokikake.Deferred.Result;
import com.example.yamaguchi.tokikake.TokikakeAnnotation.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by yamaguchi on 15/01/05.
 */
public class TaskFactory<RE extends Result> {

    public TaskFactory() {

    }
    
    public Callable<RE> createCallable(final Object obj, final Method m){
        Callable<RE> callable = new Callable<RE>() {
            @Override public RE call() throws InterruptedException {
                RE result = null;
                
                try {
                    result = (RE) m.invoke(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                return result;
            }
        };
        
        return callable;
    }

    public FutureTask<RE> createTask(final Object obj, String methodName, String callableName) {
        final Method callableMethod = ReflectionUtil.getMethod(obj, callableName);
        final Callable callable = createCallable(obj, callableMethod);
        final Method callbackMethod = ReflectionUtil.getMethod(obj, methodName);

        return new FutureTask<RE>(callable) {
            @Override
            protected void done() {
                
                try {
                    // resultにREを返す
                    RE result = this.get();
                    callbackMethod.invoke(obj, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

}
