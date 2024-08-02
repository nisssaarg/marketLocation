import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class MetadataConsumer {
    private static final String THUMBNAIL = "Thumbnail";
    private static final String PROCESSING_METADATA = "Processing metadata: ";
    private static final String CONSUMED_MESSAGE_WITH_KEY_S_AND_VALUE_S_FROM_PARTITION_D_AT_OFFSET_D_N = "Consumed message with key %s and value %s from partition %d at offset %d%n";
    private static final String OUTPUT = "output";
    private static final String EARLIEST = "earliest";
    private static final String ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String METADATA_CONSUMER_GROUP = "metadata-consumer-group";
    private static final String LOCALHOST_9092 = "localhost:9092";
    private final KafkaConsumer<String, String> consumer;
    private final String topic;

    public MetadataConsumer(String topic) {
        this.topic = topic;
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, LOCALHOST_9092);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, METADATA_CONSUMER_GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_DESERIALIZER);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_DESERIALIZER);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);


        this.consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(this.topic));
    }

    public void consume() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(OUTPUT);
                    System.out.printf(CONSUMED_MESSAGE_WITH_KEY_S_AND_VALUE_S_FROM_PARTITION_D_AT_OFFSET_D_N,
                            record.key(), record.value(), record.partition(), record.offset());
                    
                    
                    processMetadata(record.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    private void processMetadata(String metadataJson) {
        
        System.out.println(PROCESSING_METADATA + metadataJson);
    }

    public static void main(String[] args) {
        MetadataConsumer consumer = new MetadataConsumer(THUMBNAIL);
        consumer.consume();
    }
}
