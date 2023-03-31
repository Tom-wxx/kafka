package com.home.kafka.flink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.Properties;

public class FlinkKafkaProducerOne {

    public static void main(String[] args) throws Exception {

        //1.获取环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(3);  //生产的主题有几个分区

        //2.准备数据源

        ArrayList<String> wordList = new ArrayList<>();
        wordList.add("hello");
        wordList.add("cs test");
        DataStreamSource<String> stringDataStreamSource = env.fromCollection(wordList);

        //创建一个kafka生产者
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.16.22.202:9092,172.16.22.202:9093,172.16.22.202:9094");
                                                                       //  主题       序列化和反序列化模板类       数据配置信息
        FlinkKafkaProducer<String> first = new FlinkKafkaProducer<>("first", new SimpleStringSchema(), properties);

        //3.添加数据源

        stringDataStreamSource.addSink(first);  //输出目的地

        //4.执行代码

        env.execute();
    }
}
