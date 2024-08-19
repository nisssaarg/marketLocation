import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class Queue {
    private Properties properties;
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    //private Gson gson;
    private String topic = "Thumbnail";
    private int i =0;

    public Queue() {
        properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("group.id", "thumbnail-consumer-group");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        producer = new KafkaProducer<>(properties);
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public synchronized void enqueue(Map<String,String> rs) {
        try {
            String message = rs.toString();
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);

            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        exception.printStackTrace();
                    } else {
                        System.out.println("Message sent to topic" + metadata.topic() + "Partition" + metadata.partition() + "Offset" + metadata.offset());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        producer.close();
    }

    public synchronized String dequeue() {
        try{
            while(true){
                System.out.println("Polling for messages...");
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) {
                    System.out.println("No records found.");
                }
                for (ConsumerRecord<String, String> record : records) {
                    i++;
                    System.out.println("Dequeue count: " + i);
                    System.out.println("Record received: " + record.value());
                    return record.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
