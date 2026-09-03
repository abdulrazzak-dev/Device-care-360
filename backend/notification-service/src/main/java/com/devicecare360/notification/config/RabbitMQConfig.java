package com.devicecare360.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "devicecare.events";
    public static final String NOTIF_QUEUE = "notification.events.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notifQueue() {
        return new Queue(NOTIF_QUEUE, true);
    }

    @Bean
    public Binding bookingCreatedBinding(Queue notifQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notifQueue).to(exchange).with("booking.created");
    }

    @Bean
    public Binding bookingConfirmedBinding(Queue notifQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notifQueue).to(exchange).with("booking.confirmed");
    }

    @Bean
    public Binding bookingCancelledBinding(Queue notifQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notifQueue).to(exchange).with("booking.cancelled");
    }

    @Bean
    public Binding paymentCompletedBinding(Queue notifQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notifQueue).to(exchange).with("payment.completed");
    }

    @Bean
    public Binding highRiskBinding(Queue notifQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notifQueue).to(exchange).with("troubleshooting.highrisk");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
