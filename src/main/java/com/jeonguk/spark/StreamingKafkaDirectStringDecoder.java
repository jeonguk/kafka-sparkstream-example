package com.jeonguk.spark;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.serializer.StringDecoder;
import scala.Tuple2;

public class StreamingKafkaDirectStringDecoder {

    private static final Logger logger = LoggerFactory.getLogger(StreamingKafkaDirectStringDecoder.class);

    public static void main(String[] args) throws InterruptedException {

        if (args.length != 1) {
            logger.info("Usage: StreamingKafkaDirectStringDecoder <broker>");
            System.exit(1);
        }

        // Create a Java Streaming Context with a Batch Interval of 5 seconds
        SparkConf conf = new SparkConf().setAppName("Kafka Direct String Decoder");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));

        // Specify the Kafka Broker Options and set of Topics
        String broker = args[0];
        Map<String, String> kafkaParameters = new HashMap<>();
        kafkaParameters.put("metadata.broker.list", broker);
        Set<String> topics = Collections.singleton("log-topic");

        // Create an input DStream using KafkaUtils and simple plain-text message
        // processing
        JavaPairInputDStream<String, String> kafkaDirectStream = KafkaUtils.createDirectStream(jssc, String.class,
                String.class, StringDecoder.class, StringDecoder.class, kafkaParameters, topics);
        
        // kafkaDirectStream.foreachRDD(rdd -> rdd.foreach(record -> logger.info(record._2)));

        JavaDStream<String> valueDStream = kafkaDirectStream.map(new Function<Tuple2<String, String>, String>() {
            private static final long serialVersionUID = 224572829975661226L;
            public String call(Tuple2<String, String> v1) throws Exception {
                return v1._2();
            }
        });
        
        valueDStream.count().print();
        
        // Start the computation
        jssc.start();

        // Wait for the computation to terminate
        jssc.awaitTermination();
    }

}