package com.ww.cache;

import com.github.benmanes.caffeine.cache.*;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class CacheDemo {

    //1.手工设置缓存
    @Test
    public void t() throws InterruptedException {
        Cache<String, Object> cache = Caffeine.newBuilder()//构建caffeine实例
                .maximumSize(20)//设置缓存中最大数据量
                .expireAfterAccess(2L, TimeUnit.SECONDS)//设置失效时间
                .build();

        cache.put("t1", 1);//设置缓存
//        cache.put("t2", 2);
//        cache.put("t3", 3);
//        cache.put("t4", 4);

        Thread.sleep(3000);

        Object t1 = cache.getIfPresent("t1");//获取数据
        log.info("t1={}", t1);


        // 也可以使用 get 方法获取值，该方法将一个参数为 key 的 Function 作为参数传入。如果缓存中不存在该 key
        // 则该函数将用于提供默认值，该值在计算后插入缓存中：
        Object t11 = cache.get("t1", new Function<String, Object>() {
            @Override
            public Object apply(String s) {
                return 111;
            }
        });
        log.info("t1={}", t11);
    }

    //2.同步加载缓存数据
    //实际应用：项目中，利用这个同步机制，也就是在CacheLoader对象中的load函数中，当从Caffeine缓存中取不到数据的时候则从数据库中读取数据，通过这个机制和数据库结合使用
    @Test
    public void t2() throws InterruptedException {
        LoadingCache<Integer, Integer> cache = Caffeine.newBuilder()//构建caffeine实例
                .maximumSize(20)//设置缓存中最大数据量
                .expireAfterAccess(2L, TimeUnit.SECONDS)//设置失效时间
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        log.info("执行了....业务中，查询数据库，返回值赋予上去");
                        return 8;
                    }
                });
        int key1 = 1;

        cache.put(key1, 1);//设置缓存
//        Thread.sleep(3000);
//        Object t1 = cache.getIfPresent(key1);//获取数据
//        log.info("key1={}", t1);

        // get数据，取不到则从数据库中读取相关数据，该值也会插入缓存中：
        Integer value1 = cache.get(key1);
        System.out.println(value1);

        // 支持直接get一组值，支持批量查找
        Map<Integer, Integer> dataMap = cache.getAll(Arrays.asList(2, 3, 4));
        System.out.println(dataMap);
    }

    //3.异步缓存加载
    @Test
    public void t3() throws ExecutionException, InterruptedException {
        AsyncLoadingCache<Object, Object> asyncCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(2L, TimeUnit.SECONDS)
//                .buildAsync(new CacheLoader<Object, Object>() {
//                    @Override
//                    public @Nullable Object load(@NonNull Object key) throws Exception {
//                        log.info("异步任务执行了....业务中，查询数据库，返回值赋予上去");
//                        return "hahaha";
//                    }
//                });//构建接口实例
        .buildAsync((key, executor) ->
             CompletableFuture.supplyAsync(() -> {
                 log.info("异步任务执行了....业务中，查询数据库，返回值赋予上去");
                 return "888" + key;
             })
        );

        //异步缓存管理，所以需要异步设置缓存项
        asyncCache.put("t1", CompletableFuture.completedFuture("呵呵呵~"));//获取一个 执行状态已经完成 的 CompletableFuture 对象。

        Thread.sleep(3000);

        //获取数据
        CompletableFuture<Object> t1 = asyncCache.get("t1");
        System.out.println(t1.get());
    }


    /**
     * 模拟从数据库中读取key
     *
     * @param key
     * @return
     */
    private int getInDB(int key) {
        return key + 1;
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        // 使用executor设置线程池
        AsyncCache<Integer, Integer> asyncCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .maximumSize(100)
                .executor(Executors.newSingleThreadExecutor())// 设置执行器
                .buildAsync();

        Integer key = 1;

        // get返回的是CompletableFuture
        CompletableFuture<Integer> future = asyncCache.get(key, new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer key) {
                // 执行所在的线程不在是main，而是ForkJoinPool线程池提供的线程
                System.out.println("apply() 当前所在线程：" + Thread.currentThread().getName());
                return getInDB(key);
            }
        });

        int value = future.get();

        System.out.println("当前所在线程：" + Thread.currentThread().getName());
        System.out.println(value);
    }



}
