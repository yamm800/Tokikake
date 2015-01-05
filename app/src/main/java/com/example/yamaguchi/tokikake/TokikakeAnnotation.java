package com.example.yamaguchi.tokikake;

import android.util.Log;

import com.example.yamaguchi.tokikake.Deferred.Result;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Created by yamaguchi on 15/01/05.
 */
public class TokikakeAnnotation {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface MyAnnotation {
        public String value();
    }

    public static class ReflectionUtil<RE extends Result> {
        public static Method getMethod(final Object obj, String value) {
            if (obj == null) {
                return null;
            }

            final Class cls = obj.getClass();

            final Method[] methodList = cls.getDeclaredMethods();
            
            Method method = null;

            for (Method m : methodList) {
                m.setAccessible(true);

                String name = m.getName();

                if (m.getAnnotation(MyAnnotation.class) != null) {
                    MyAnnotation element =
                            m.getAnnotation(MyAnnotation.class);
                    Log.d("TAG", element.value());

                    if (element.value().equals(value)) {
                        method = m;
                        break;
                    }
                }
            }
            
            return method;
        }
    }
}
