package com.home.kafka.partitioner;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;
/**
 * 根据value含有不同的值，分别发送到不同的分区*/
public class MyPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] bytes, Object value, byte[] bytes1, Cluster cluster) {
        //获取数据  wxx , zsx
        String msgValue=value.toString();
        int partition;
        if (msgValue.contains("wxx")){
            partition=0;
        }else {
            partition=1;
        }
        return partition;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
