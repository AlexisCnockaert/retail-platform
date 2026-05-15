package com.retail.order;

import com.retail.common.ResourceNotFoundException;
import com.retail.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderResponse placeOrder(PlaceOrderRequest request) {

        List<OrderLineItem> items = request.items().stream()
                .map(i -> new OrderLineItem(
                        i.productId(),
                        i.sku(),
                        i.quantity(),
                        i.unitPrice()
                ))
                .toList();

        BigDecimal total = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        var order = new Order(
                null,
                request.customerId(),
                items,
                total,
                OrderStatus.PENDING,
                null // @CreatedDate
        );

        Order saved = orderRepository.save(order);
        log.info("Order saved: id={} customerId={} total={}", saved.id(), saved.customerId(), saved.totalAmount());

        var event = new OrderEvent(
                saved.id(),
                saved.customerId(),
                saved.items(),
                saved.totalAmount(),
                saved.placedAt()
        );

        kafkaTemplate.send(KafkaTopics.ORDER_PLACED, saved.id(), event);
        log.info("Event published: topic={} orderId={}", KafkaTopics.ORDER_PLACED, saved.id());

        return OrderResponse.from(saved);
    }

    public OrderResponse findById(String id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public Page<OrderResponse> findByCustomer(String customerId, int page, int size) {
        return orderRepository
                .findByCustomerId(customerId, PageRequest.of(page, size))
                .map(OrderResponse::from);
    }

    public void confirmOrder(String orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            var confirmed = new Order(
                    order.id(), order.customerId(), order.items(),
                    order.totalAmount(), OrderStatus.CONFIRMED, order.placedAt()
            );
            orderRepository.save(confirmed);
            log.info("Order confirmed: id={}", orderId);
        });
    }

    public void cancelOrder(String orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            var cancelled = new Order(
                    order.id(), order.customerId(), order.items(),
                    order.totalAmount(), OrderStatus.CANCELLED, order.placedAt()
            );
            orderRepository.save(cancelled);
            log.warn("Order cancelled (insufficient stock): id={}", orderId);
        });
    }
}
