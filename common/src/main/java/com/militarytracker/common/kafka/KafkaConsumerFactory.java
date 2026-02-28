package com.militarytracker.common.kafka;

import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public final class KafkaConsumerFactory {

    private KafkaConsumerFactory() {
    }

    public static KafkaConsumer<String, String> create(Config kafkaConfig) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getString("bootstrap-servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getString("consumer-group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        if (kafkaConfig.hasPath("ssl.enabled") && kafkaConfig.getBoolean("ssl.enabled")) {
            props.put("security.protocol", "SSL");
            if (kafkaConfig.hasPath("ssl.truststore.location")) {
                props.put("ssl.truststore.location", kafkaConfig.getString("ssl.truststore.location"));
                props.put("ssl.truststore.password", kafkaConfig.getString("ssl.truststore.password"));
            }
            if (kafkaConfig.hasPath("ssl.keystore.location")) {
                props.put("ssl.keystore.location", kafkaConfig.getString("ssl.keystore.location"));
                props.put("ssl.keystore.password", kafkaConfig.getString("ssl.keystore.password"));
            }
        }

        return new KafkaConsumer<>(props);
    }

    public static Properties buildProperties(Config kafkaConfig) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getString("bootstrap-servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getString("consumer-group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }
}
