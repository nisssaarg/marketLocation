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
    private static final String RECORD_RECEIVED = "Record received: ";
    private static final String DEQUEUE_COUNT = "Dequeue count: ";
    private static final String NO_RECORDS_FOUND = "No records found.";
    private static final String POLLING_FOR_MESSAGES = "Polling for messages...";
    private static final String OFFSET = "Offset";
    private static final String PARTITION = "Partition";
    private static final String MESSAGE_SENT_TO_TOPIC = "Message sent to topic";
    private static final String VALUE_DESERIALIZER = "value.deserializer";
    private static final String ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String KEY_DESERIALIZER = "key.deserializer";
    private static final String THUMBNAIL_CONSUMER_GROUP = "thumbnail-consumer-group";
    private static final String GROUP_ID = "group.id";
    private static final String VALUE_SERIALIZER = "value.serializer";
    private static final String ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String KEY_SERIALIZER = "key.serializer";
    private static final String LOCALHOST_9092 = "localhost:9092";
    private static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    private Properties properties;
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    //private Gson gson;
    private String topic = "Thumbnail";
    private int i =0;

    public Queue() {
        properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS, LOCALHOST_9092);
        properties.put(KEY_SERIALIZER, ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_SERIALIZER);
        properties.put(VALUE_SERIALIZER, ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_SERIALIZER);
        properties.put(GROUP_ID, THUMBNAIL_CONSUMER_GROUP);
        properties.put(KEY_DESERIALIZER, ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_DESERIALIZER);
        properties.put(VALUE_DESERIALIZER, ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_DESERIALIZER);

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
                        System.out.println(MESSAGE_SENT_TO_TOPIC + metadata.topic() + PARTITION + metadata.partition() + OFFSET + metadata.offset());
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
                System.out.println(POLLING_FOR_MESSAGES);
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) {
                    System.out.println(NO_RECORDS_FOUND);
                }
                for (ConsumerRecord<String, String> record : records) {
                    i++;
                    System.out.println(DEQUEUE_COUNT + i);
                    System.out.println(RECORD_RECEIVED + record.value());
                    return record.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
