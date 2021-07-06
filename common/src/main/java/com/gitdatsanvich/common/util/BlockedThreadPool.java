package com.gitdatsanvich.common.util;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 阻塞线程池
 *
 * @author TangChen
 * @date 2019/11/13 9:50
 **/

@Component
public class BlockedThreadPool<T> {

    private static final Logger logger = LoggerFactory.getLogger(BlockedThreadPool.class);

    protected ThreadPoolExecutor pool = null;

    private static final int CORE_POOL_SIZE = 10;

    private static final int MAXIMUM_POOL_SIZE = 15;

    private static final int KEEP_ALIVE_TIME = 5;

    protected TimeUnit keepAliveTimeUnit = TimeUnit.MINUTES;

    private final int capacity = 100;

    protected int timeout = 600000;

    protected TimeUnit timeoutUnit = TimeUnit.SECONDS;

    @PostConstruct
    protected void init() {
        init(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, keepAliveTimeUnit, capacity);
    }

    protected void init(int corePoolSize, int maximumPoolSize, int keepAliveTime, TimeUnit timeUnit, int capacity) {
        pool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                timeUnit,
                new ArrayBlockingQueue<>(capacity),
                new CustomThreadFactory(),
                new CustomRejectedExecutionHandler());
        logger.info("corePoolSize:" + pool.getCorePoolSize() + ",maximumPoolSize:" + pool.getMaximumPoolSize() + ",keepAliveTime:" + pool.getKeepAliveTime(timeUnit));
    }

    /**
     * 获取当前线程池活动线程数
     */
    public int activeThreadNumber() {
        return pool.getActiveCount();
    }

    /**
     * 线程池关闭
     */
    private void destroy() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    /**
     * 检查线程池
     */
    public void checkPool() {
        if (pool == null) {
            this.init();
        }
    }

    /**
     * 当前状态获取并返回参数
     */
    private List<T> futureMonitoring(List<Future<T>> futureList) {
        List<T> returnList = new ArrayList<>();
        logger.info("清理线程池线程");
        //遍历状态列表
        for (Future<T> future : futureList) {
            //检查状态
            try {
                T o = future.get(timeout, timeoutUnit);
                returnList.add(o);
            } catch (TimeoutException e) {
                logger.error("线程池 子线程被超时", e);
            } catch (CancellationException e) {
                logger.error("线程池 子线程被取消", e);
            } catch (InterruptedException ex) {
                logger.error("线程池 子线程被中断", ex);
            } catch (ExecutionException e) {
                logger.error("线程池 子线程执行出错", e);
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("线程池 线程执行未知错误");
            }
        }
        return returnList;
    }

    /**
     * 同步运行线程池
     *
     * @param taskList 运行的方法集合
     * @return 获取Callable返回的值
     */
    public List<T> submitAllSynchronous(List<Callable<T>> taskList) {
        /*同步运行的线程*/
        List<Future<T>> futureList = new LinkedList<>();
        for (Callable<T> task : taskList) {
            //添加任务
            Future<T> submit = pool.submit(task);
            //添加同步监控
            futureList.add(submit);
        }
        return this.futureMonitoring(futureList);
    }

    /**
     * 异步运行线程池
     *
     * @param taskList taskList
     */
    public void submitAllAsynchronous(List<Runnable> taskList) {
        /*异步运行的线程 暂时不会获取返回值*/
        List<Future<?>> futureList = new LinkedList<>();
        for (Runnable task : taskList) {
            Future<?> submit = pool.submit(task);
            futureList.add(submit);
        }
        Runnable r = () -> futureMonitoringAsynchronous(futureList);
        pool.submit(r);
    }

    private void futureMonitoringAsynchronous(List<Future<?>> futureList) {
        for (Future<?> future : futureList) {
            try {
                future.get(timeout, timeoutUnit);
            } catch (TimeoutException e) {
                logger.error("线程池 子线程被超时", e);
            } catch (CancellationException e) {
                logger.error("线程池 子线程被取消", e);
            } catch (InterruptedException ex) {
                logger.error("线程池 子线程被中断", ex);
            } catch (ExecutionException e) {
                logger.error("线程池 子线程执行出错", e);
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("线程池 线程执行未知错误");
            }
        }
    }

    protected static class CustomThreadFactory implements ThreadFactory {

        private final AtomicLong count = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            String threadName = BlockedThreadPool.class.getSimpleName() + count.addAndGet(1);
            logger.info(threadName);
            t.setName(threadName);
            return t;
        }
    }

    protected class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

        @SneakyThrows
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                logger.info("阻塞拒绝直接执行");
                logger.info(Thread.currentThread().getName());
                /*等待1秒*/
                Thread.sleep(1000);
                /*重新提交到线程池*/
                pool.submit(r);
            }
        }
    }
}