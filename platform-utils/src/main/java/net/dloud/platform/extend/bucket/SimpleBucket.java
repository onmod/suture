package net.dloud.platform.extend.bucket;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-12-10 15:17
 **/
public class SimpleBucket implements Bucket {
    private List<Bandwidth> bandwidths;


    private SimpleBucket(List<Bandwidth> bandwidths) {
        this.bandwidths = bandwidths;
    }

    public static SimpleBucket build(List<Bandwidth> bandwidths) {
        return new SimpleBucket(bandwidths);
    }

    public static SimpleBucket build(Bandwidth... bandwidths) {
        List<Bandwidth> list = Lists.newArrayListWithExpectedSize(bandwidths.length);
        list.addAll(Arrays.asList(bandwidths));
        return new SimpleBucket(list);
    }

    @Override
    public boolean tryConsume(long numTokens) {
        boolean res = true;
        long now = System.currentTimeMillis();
        for (Bandwidth bandwidth : bandwidths) {
            long initial = bandwidth.getInitial();
            if (now < initial) {
                //已消费完毕
                res = false;
            } else {
                long consumed = bandwidth.getConsumed();
                long interval = bandwidth.getInterval();
                long diff = now - initial;
                if (diff >= interval) {
                    consumed = 0;
                    initial += (diff / interval) * interval;
                }

                long capacity = bandwidth.getCapacity();
                if (numTokens > capacity) {
                    //要消费的比负载大
                    res = false;
                } else {
                    if (numTokens + consumed > capacity) {
                        //超出限额无法消费
                        res = false;
                    } else {
                        if (res) {
                            consumed += numTokens;
                        }
                    }
                }
                bandwidth.setInitial(initial);
                bandwidth.setConsumed(consumed);
            }
        }
        return res;
    }

    @Override
    public void addTokens(long tokensToAdd) {
        for (Bandwidth bandwidth : bandwidths) {
            long consumed = bandwidth.getConsumed();
            if (consumed > 0) {
                consumed -= tokensToAdd;
            }
            if (consumed < 0) {
                consumed = 0;
            }
            bandwidth.setConsumed(consumed);
        }
    }

    @Override
    public List<Bandwidth> getBandwidths() {
        return bandwidths;
    }
}
