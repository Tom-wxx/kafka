package com.home.kafka.producer;

import com.home.kafka.partitioner.MyPartitioner;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Slf4j
public class CustomProducer {

    public static void main(String[] args) {
        //0.配置
        Properties properties = new Properties();

        //连接集群
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.16.22.202:9092,172.16.22.202:9093,172.16.22.202:9094");
        //指定对应的key和序列化类型
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        /**关联自定义分区器*/
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,"com.home.kafka.partitioner.MyPartitioner");

        /**提高吞吐量  生产环境中需灵活配置*/
        //缓冲区 默认 32M -> 64M
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG,33554432);
        //批次大小 默认16k->32K
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG,16384);
        //linger.ms 默认 0  生产环境为 5-100毫秒之间 根据具体的业务
        properties.put(ProducerConfig.LINGER_MS_CONFIG,1);
        //压缩
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,"snappy");

        /**数据可靠性  副本 只针对leader操作   应答
         * ack：
         *      0：无需数据楼盘应答  很少使用
         *
         *      1：leader收到数据后应答    一般用于传输普通日志
         * 0，1存在丢数风险
         *      -1：leader和所有 isr 节点follower收齐数据后应答   一般用于传输和钱相关的数据
         *          存在数据重复问题
         *      */

        properties.put(ProducerConfig.ACKS_CONFIG,"all");
        //重试次数  默认 int 最大值 0 - 2147483647
        properties.put(ProducerConfig.RETRIES_CONFIG,3);

        /**使用事务 必须指定全局事务id*/
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,"tranactional_id_01");


        //1.创建kafka生产者对象
        KafkaProducer<String,String> kafkaProducer = new KafkaProducer<>(properties);
        /**使用事务*/
        kafkaProducer.initTransactions(); //初始化事务
        kafkaProducer.beginTransaction();  //开启事务
        try {

            //2.发送数据
            for (int i = 0; i < 10; i++) {
                sendMsgPartition(kafkaProducer,i);
            }
            kafkaProducer.commitTransaction(); //提交事务

        }catch (Exception e){
            kafkaProducer.abortTransaction(); //终止事务
            e.printStackTrace();

        }finally {
            kafkaProducer.close();
        }

    }

    //异步发送：简单发送
    private static void sendMsg(KafkaProducer<String, String> kafkaProducer, Integer i){
        kafkaProducer.send(new ProducerRecord<>("hello-kafka-test-topic","test Message"+i));
    }
    //异步发送： 回调函数
    private static void sendMsgCallBack(KafkaProducer<String, String> kafkaProducer, Integer i){
        kafkaProducer.send(new ProducerRecord<>("hello-kafka-test-topic", "test Message" + i), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e==null){
                    System.out.println("主题："+recordMetadata.topic()+"    分区："+recordMetadata.partition());
                }
            }
        });
    }

    //同步发送： 在异步发送的基础上加异常处理
    private static void sendMsgGet(KafkaProducer<String, String> kafkaProducer, Integer i){
        try {
            kafkaProducer.send(new ProducerRecord<>("hello-kafka-test-topic","test Message"+i)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
        }
    }

    //异步发送： 回调函数  指定分区：自定义
    private static void sendMsgPartition(KafkaProducer<String, String> kafkaProducer, Integer i){
        kafkaProducer.send(new ProducerRecord<>("hello-kafka-test-topic",1,"", "test Message" + i), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e==null){
                    System.out.println("主题："+recordMetadata.topic()+"    分区："+recordMetadata.partition());
                }
            }
        });
    }
    //               不指定会根据key的hash值取余找到分区值
    //不指定分区或key  采取黏性：自动分区
    private static void sendMsgPartitionKey(KafkaProducer<String, String> kafkaProducer, Integer i){
        kafkaProducer.send(new ProducerRecord<>("hello-kafka-test-topic","b", "test Message" + i), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e==null){
                    log.info("主题："+recordMetadata.topic()+"    分区："+recordMetadata.partition());
                }
            }
        });
    }

    //使用分区器 来发送对应的分区  与上面关联自定义分区器 对应
    private static void sendMsgPartitionDifferent(KafkaProducer<String, String> kafkaProducer, Integer i){
        kafkaProducer.send(new ProducerRecord<>("hello-kafka-test-topic", "xlkkk test Message" + i), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e==null){
                    System.out.println("主题："+recordMetadata.topic()+"    分区："+recordMetadata.partition());
                }
            }
        });
    }

    /**提高吞吐量*/



}
