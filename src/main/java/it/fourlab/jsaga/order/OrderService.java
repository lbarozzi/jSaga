package it.fourlab.jsaga.order;

import it.fourlab.jsaga.common.ResourceNotFoundException;
import it.fourlab.jsaga.printing.EscPosPrinterException;
import it.fourlab.jsaga.printing.EscPosPrintService;
import it.fourlab.jsaga.printing.PrintLine;
import it.fourlab.jsaga.product.Product;
import it.fourlab.jsaga.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EscPosPrintService printService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        EscPosPrintService printService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.printService = printService;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        List<OrderItem> items = resolveItems(request.items());
        BigDecimal total = computeTotal(items);

        Order order = new Order();
        order.setEventId(request.eventId());
        order.setTotalAmount(total);
        order.setPaymentMethod(request.paymentMethod());
        order.setCreatedAt(LocalDateTime.now());
        items.forEach(i -> {
            i.setOrder(order);
            order.getItems().add(i);
        });

        Order saved = orderRepository.save(order);
        boolean printed = tryPrint(saved);
        return toResponse(saved, printed);
    }

    @Transactional(readOnly = true)
    public void printOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        printService.printOrder("ORATORIO - GESTIONALE FESTA", toLines(order.getItems()), order.getTotalAmount(),order.getId());
    }

    private List<OrderItem> resolveItems(List<OrderItemRequest> requests) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest req : requests) {
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.productId()));
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQty(req.qty());
            item.setUnitPrice(product.getPrice());
            items.add(item);
        }
        return items;
    }

    private BigDecimal computeTotal(List<OrderItem> items) {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean tryPrint(Order order) {
        try {
            printService.printOrder("ORATORIO - GESTIONALE FESTA", toLines(order.getItems()), order.getTotalAmount(),order.getId());
            return true;
        } catch (EscPosPrinterException e) {
            log.warn("Stampa non riuscita per ordine {}: {}", order.getId(), e.getMessage());
            return false;
        }
    }

    private static List<PrintLine> toLines(List<OrderItem> items) {
        return items.stream()
                .map(i -> new PrintLine(i.getQty(), i.getProductName(), i.getUnitPrice()))
                .toList();
    }

    private static OrderResponse toResponse(Order order, boolean printed) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProductId(),
                        i.getProductName(),
                        i.getQty(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty()))))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getEventId(),
                itemResponses,
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                printed);
    }
}
