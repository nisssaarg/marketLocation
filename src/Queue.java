import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.Callback;

class Queue {
    private static final String OFFSET = " offset ";
    private static final String PARTITION = " partition ";
    private static final String MESSAGE_SENT_TO_TOPIC = "Message sent to topic ";
    private static final String ALL = "all";
    private static final String ACKS = "acks";
    private static final String VALUE_SERIALIZER = "value.serializer";
    private static final String ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String KEY_SERIALIZER = "key.serializer";
    private static final String LOCALHOST_9092 = "localhost:9092";
    private static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final ObjectMapper objectMapper;

    public Queue(String topic) {
        this.topic = topic;
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS, LOCALHOST_9092); // Change to your Kafka server
        props.put(KEY_SERIALIZER, ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_SERIALIZER);
        props.put(VALUE_SERIALIZER, ORG_APACHE_KAFKA_COMMON_SERIALIZATION_STRING_SERIALIZER);
        props.put(ACKS, ALL);
        this.producer = new KafkaProducer<>(props);
        this.objectMapper = new ObjectMapper();

    }

    public void enqueue(Map<String,String> rs) {
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
}
