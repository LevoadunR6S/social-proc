package org.micro.shareable.kafka;

import lombok.Data;
import org.micro.shareable.dto.UserDto;

//Клас, який є моделлю повідомлення в Kafka
@Data
public class KafkaMessage {
    private String username;
    private UserDto userDto;
    private String info;


    public KafkaMessage(String username) {
        this.username = username;
    }

    public KafkaMessage(UserDto user, String info) {
        this.userDto = user;
        this.info = info;
    }

    public KafkaMessage(UserDto user) {
        this.userDto = user;
    }

    public KafkaMessage() {
    }

    ;
}
