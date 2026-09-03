package com.devicecare360.technician.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "devicecare.events";
    public static final String TECH_REGISTERED_QUEUE = "technician.service.registered.queue";
    public static final String REVIEW_CREATED_QUEUE = "technician.service.review.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue techRegisteredQueue() {
        return new Queue(TECH_REGISTERED_QUEUE, true);
    }

    @Bean
    public Queue reviewCreatedQueue() {
        return new Queue(REVIEW_CREATED_QUEUE, true);
    }

    @Bean
    public Binding techRegisteredBinding(TopicExchange exchange) {
        return BindingBuilder.bind(techRegisteredQueue()).to(exchange).with("technician.registered");
    }

    @Bean
    public Binding reviewCreatedBinding(TopicExchange exchange) {
        return BindingBuilder.bind(reviewCreatedQueue()).to(exchange).with("review.created");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
