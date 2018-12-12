package net.dloud.platform.parse.redisson.dataccess;

import org.redisson.api.RLock;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author QuDasheng
 * @create 2018-12-05 09:25
 **/
public class WrapperLock implements Lock, Closeable {
    private final RLock lock;


    public WrapperLock(RLock lock) {
        this.lock = lock;
    }

    @Override
    public void close() throws IOException {
        lock.unlock();
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return lock.tryLock(time, unit);
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public Condition newCondition() {
        return lock.newCondition();
    }
}
