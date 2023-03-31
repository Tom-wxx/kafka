package com.home.kafka.flink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.ArrayList;
import java.util.Properties;

public class FlinkKafkaConsumerOne {

    public static void main(String[] args) throws Exception {
        //1.获取环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(3);  //生产的主题有几个分区

        //创建一个kafka消费者
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.16.22.202:9092,172.16.22.202:9093,172.16.22.202:9094");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"test");
        //  主题       序列化和反序列化模板类       数据配置信息
        FlinkKafkaConsumer<String> first = new FlinkKafkaConsumer<>("first", new SimpleStringSchema(), properties);

        //3.关联消费者 和 flink
        env.addSource(first).print();

        //4.执行代码

        env.execute();
    }
}
