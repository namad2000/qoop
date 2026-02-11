package io.qoop.stream.subscriber.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class TestConsumer {

    private final AtomicInteger counter = new AtomicInteger();
    public final BlockingQueue<String> mainQueue = new LinkedBlockingQueue<>();

    @KafkaListener(topics = "test-topic", containerFactory = "kafkaListenerContainerFactory")
    public void consume(
            String msg,
            Acknowledgment ack,
            ConsumerRecord<String, String> record
    ) {

        counter.incrementAndGet();

        throw new RuntimeException("fail processing");
    }
}
