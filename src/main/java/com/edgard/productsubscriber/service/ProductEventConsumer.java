package com.edgard.productsubscriber.service;

import com.edgard.productsubscriber.model.Envelope;
import com.edgard.productsubscriber.model.ProductEvent;
import com.edgard.productsubscriber.model.ProductEventLog;
import com.edgard.productsubscriber.model.SnsMessage;
import com.edgard.productsubscriber.repository.ProductEventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Service
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductEventLogRepository repository;

    @JmsListener(destination = "${aws.sqs.queue.product.events.name}")
    public void receiveProductEvent(TextMessage textMessage) throws JMSException, IOException {

        SnsMessage snsMessage = objectMapper.readValue(textMessage.getText(), SnsMessage.class);

        Envelope envelope = objectMapper.readValue(snsMessage.getMessage(), Envelope.class);

        ProductEvent productEvent = objectMapper.readValue(envelope.getData(), ProductEvent.class);

        log.info("Product event received - MessageId: {} - Message: {} - ", snsMessage.getMessageId(), snsMessage.getMessage());

        ProductEventLog productEventLog = buildProductEventLog(envelope, productEvent);
        repository.save(productEventLog);

    }

    private ProductEventLog buildProductEventLog(Envelope envelope, ProductEvent productEvent) {
        Instant now = Instant.now();

        ProductEventLog productEventLog = new ProductEventLog();
        productEventLog.setPk(productEvent.getProductCode());
        productEventLog.setSk(envelope.getType() + "_" + now.toEpochMilli());
        productEventLog.setEventType(envelope.getType());
        productEventLog.setProductId(productEvent.getProductId());
        productEventLog.setUsername(productEvent.getUsername());
        productEventLog.setTimestamp(now.toEpochMilli());
        productEventLog.setTtl(now.plus(Duration.ofMinutes(10)).getEpochSecond());

        return productEventLog;
    }

}
