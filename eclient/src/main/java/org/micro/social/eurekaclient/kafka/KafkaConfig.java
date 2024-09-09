package org.micro.social.eurekaclient.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;


//Клас для конфігурації Kafka
@Configuration
@EnableKafka
public class KafkaConfig {


    //Налаштування producer
    @Bean
    public ProducerFactory<String, Object> producerFactoryUserService() {
        Map<String, Object> configProps = new HashMap<>();
        //Вказуємо до якого сервера підключається продюсер
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        //Вказуємо як буде серіалізовуватись ключ в повідомленні Kafka
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //Вказуємо як буде серіалізовуватись значення в повідомленні Kafka
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }


    //Налаштування consumer
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        //Вказуємо до якого сервера підключається продюсер
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        //Вказуємо групу консюмерів
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "user-requests");
        //Вказуємо як буде десеріалізовуватись ключ в повідомленні Kafka
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        //Вказуємо як буде десеріалізовуватись значення в повідомленні Kafka
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Установлюємо auto.offset.reset
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Забороняємо автоматичне комітування офсетів

        //Вказуємо пакети з яких можна десеріалізувати об'єкти
        configProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "*");
        //Вказуємо до якого типу будуть десеріалізовуватись об'єкти по замовчуванню
        configProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class.getName());
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new org.springframework.kafka.support.serializer.JsonDeserializer<>());
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        // Створює новий екземпляр ConcurrentKafkaListenerContainerFactory для конфігурації споживачів Kafka
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // Встановлює фабрику споживачів для цієї фабрики контейнера слухачів
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }


    @Bean
    public KafkaTemplate<String, Object> userServiceKafkaTemplate() {
        return new KafkaTemplate<>(producerFactoryUserService());
    }
}
