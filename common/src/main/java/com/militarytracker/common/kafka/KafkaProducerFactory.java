package com.militarytracker.common.kafka;

import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public final class KafkaProducerFactory {

    private KafkaProducerFactory() {
    }

    public static KafkaProducer<String, String> create(Config kafkaConfig) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getString("bootstrap-servers"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, kafkaConfig.getString("acks"));

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

        return new KafkaProducer<>(props);
    }

    public static Properties buildProperties(Config kafkaConfig) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getString("bootstrap-servers"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, kafkaConfig.getString("acks"));
        return props;
    }
}
