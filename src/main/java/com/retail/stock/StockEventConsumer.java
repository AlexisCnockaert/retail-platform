package com.retail.stock;

import com.retail.config.KafkaTopics;
import com.retail.order.OrderEvent;
import com.retail.order.OrderLineItem;
import com.retail.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventConsumer {

    private final StockService stockService;
    private final OrderService orderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @KafkaListener(topics = KafkaTopics.ORDER_PLACED, groupId = "retail-group")
    public void onOrderPlaced(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received order event: orderId={} partition={} offset={}",
                event.orderId(), partition, offset);

        try {

            for (OrderLineItem item : event.items()) {
                stockService.adjust(
                        item.productId(),
                        new StockAdjustRequest(item.quantity(), AdjustDirection.OUT)
                );
                log.info("Stock decremented: productId={} qty={}", item.productId(), item.quantity());
            }

            orderService.confirmOrder(event.orderId());


            for (OrderLineItem item : event.items()) {
                var stockUpdatedEvent = new StockUpdatedEvent(
                        item.productId(),
                        item.sku(),
                        item.quantity(),
                        StockMovementReason.ORDER
                );
                kafkaTemplate.send(KafkaTopics.STOCK_UPDATED, item.productId(), stockUpdatedEvent);
            }

        } catch (IllegalArgumentException e) {
            log.warn("Insufficient stock for order {}: {}", event.orderId(), e.getMessage());
            orderService.cancelOrder(event.orderId());
        }
    }
}
