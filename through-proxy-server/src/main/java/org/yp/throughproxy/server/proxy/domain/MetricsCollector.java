package org.yp.throughproxy.server.proxy.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: yp
 * @date: 2024/8/30 11:44
 * @description:保存连接过程的数据，连接计算
 */
public class MetricsCollector {

    private static Map<Integer, MetricsCollector> metricsCollectors = new ConcurrentHashMap<>();

    private Integer port;

    private AtomicLong readBytes = new AtomicLong();

    private AtomicLong writeBytes = new AtomicLong();

    private AtomicLong readMsgs = new AtomicLong();

    private AtomicLong writeMsgs = new AtomicLong();

    private AtomicInteger channels = new AtomicInteger();

    private MetricsCollector(){

    }

    public static MetricsCollector getCollector(Integer port){
        MetricsCollector collector = metricsCollectors.get(port);
        if(collector == null){
            synchronized (metricsCollectors){
                collector = metricsCollectors.get(port);
                if(collector == null){
                    collector = new MetricsCollector();
                    metricsCollectors.put(port, collector);
                }
            }
        }
        return collector;
    }

    public static List<Metrics> getAndResetAllMetrics(){
        List<Metrics> allMetrics = new ArrayList<>();
        Iterator<Map.Entry<Integer, MetricsCollector>> iterator = metricsCollectors.entrySet().iterator();
        while(iterator.hasNext()){
            allMetrics.add(iterator.next().getValue().getAndResetMetrics());
        }
        return allMetrics;
    }



    public static List<Metrics> getAllMetrics() {
        List<Metrics> allMetrics = new ArrayList<Metrics>();
        Iterator<Map.Entry<Integer, MetricsCollector>> ite = metricsCollectors.entrySet().iterator();
        while (ite.hasNext()) {
            allMetrics.add(ite.next().getValue().getMetrics());
        }

        return allMetrics;
    }


    public Metrics getAndResetMetrics() {
        Metrics metrics = new Metrics();
        metrics.setChannels(channels.get());
        metrics.setPort(port);
        metrics.setReadBytes(readBytes.getAndSet(0));
        metrics.setWriteBytes(writeBytes.getAndSet(0));
        metrics.setTimestamps(System.currentTimeMillis());
        metrics.setReadMsgs(readMsgs.getAndSet(0));
        metrics.setWriteMsgs(writeMsgs.getAndSet(0));
        return metrics;
    }


    public Metrics getMetrics() {
        Metrics metrics = new Metrics();
        metrics.setChannels(channels.get());
        metrics.setPort(port);
        metrics.setReadBytes(readBytes.get());
        metrics.setWriteBytes(writeBytes.get());
        metrics.setTimestamps(System.currentTimeMillis());
        metrics.setReadMsgs(readMsgs.get());
        metrics.setWriteMsgs(writeMsgs.get());
        return metrics;
    }

    public void incrementReadBytes(long bytes){
        readBytes.addAndGet(bytes);
    }

    public void incrementWriteBytes(long bytes){
        writeBytes.addAndGet(bytes);
    }

    public void incrementReadMsgs(long msgs) {
        readMsgs.addAndGet(msgs);
    }

    public void incrementWriteMsgs(long msgs) {
        writeMsgs.addAndGet(msgs);
    }

    public AtomicInteger getChannels() {
        return channels;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }


}
