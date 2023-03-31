package com.home.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;

@Slf4j
public class CustomConsumer {

    public static void main(String[] args) {

        //配置
        Properties properties = new Properties();
        //连接集群
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.16.22.202:9092,172.16.22.202:9093,172.16.22.202:9094");
        //反序列化
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());

        /**必须配置消费者组id*/
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"test");

        /**自动提交 offset*/
       // properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,true);
        //提交时间间隔 1000毫秒
       // properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,1000);

        /**或者手动提交*/
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        //1.创建消费者
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);

        //2.订阅主题
       /* ArrayList<String> topics = new ArrayList<>();
        topics.add("first");
        topics.add("hello-kafka-test-topic");
        kafkaConsumer.subscribe(topics);*/

        /**订阅主题对应的分区*/
        ArrayList<TopicPartition> topicPartitions = new ArrayList<>();
   //     topicPartitions.add(new TopicPartition("first",0));
        topicPartitions.add(new TopicPartition("hello-kafka-test-topic",1));
        kafkaConsumer.assign(topicPartitions);

        /**指定位置进行消费*/
        Set<TopicPartition> assignment = kafkaConsumer.assignment();  //获取对应的分区信息
        //保障分区分配方案已经制定完毕
        while (assignment.size()==0){
            kafkaConsumer.poll(Duration.ofSeconds(1));
            assignment=kafkaConsumer.assignment();
        }

        //希望把时间转换为offset
        HashMap<TopicPartition, Long> topicPartitionLong = new HashMap<>();
        //封装对应的集合
        assignment.forEach(item->{
            topicPartitionLong.put(item,System.currentTimeMillis()-1*24*3600*1000);
        });
        Map<TopicPartition, OffsetAndTimestamp> offsetAndTimestampMap = kafkaConsumer.offsetsForTimes(topicPartitionLong);

        //指定消费的offset
        assignment.forEach(e->{
            OffsetAndTimestamp offsetAndTimestamp = offsetAndTimestampMap.get(e);
            kafkaConsumer.seek(e,offsetAndTimestamp.offset()); //从 offset 600这个位置往后进行消费
        });


        //3.消费数据
        while (true){
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(1));
            consumerRecords.forEach(e->{
                System.out.println(e);
            });
            //手动提交offset
            kafkaConsumer.commitAsync();
        }
    }
}
