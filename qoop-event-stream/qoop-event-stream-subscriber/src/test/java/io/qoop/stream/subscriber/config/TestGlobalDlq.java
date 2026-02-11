package io.qoop.stream.subscriber.config;

import io.qoop.stream.api.ErrorMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TestGlobalDlq {

    public final BlockingQueue<ErrorMessage> dlqQueue = new LinkedBlockingQueue<>();

    @KafkaListener(topics = "global-dlt-topic", containerFactory = "kafkaListenerContainerFactory")
    public void consumeDlq(ErrorMessage msg, Acknowledgment ack) {
        System.out.println("Global DLQ Consumer received: " + msg);
        dlqQueue.add(msg);
        ack.acknowledge();
    }
}
