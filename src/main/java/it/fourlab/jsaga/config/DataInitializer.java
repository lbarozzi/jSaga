package it.fourlab.jsaga.config;

import it.fourlab.jsaga.event.Event;
import it.fourlab.jsaga.event.EventRepository;
import it.fourlab.jsaga.event.EventStatus;
import it.fourlab.jsaga.product.Product;
import it.fourlab.jsaga.product.ProductCategory;
import it.fourlab.jsaga.product.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final EventRepository eventRepository;

    public DataInitializer(ProductRepository productRepository, EventRepository eventRepository) {
        this.productRepository = productRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() == 0) {
            seedProducts();
        }
        if (eventRepository.count() == 0) {
            seedEvents();
        }
    }

    private void seedProducts() {
        productRepository.saveAll(List.of(
                new Product("Panino con salsiccia",    ProductCategory.GASTRONOMIA, new BigDecimal("3.50"), true),
                new Product("Panino con porchetta",    ProductCategory.GASTRONOMIA, new BigDecimal("4.00"), true),
                new Product("Piadina vegetariana",     ProductCategory.GASTRONOMIA, new BigDecimal("3.00"), true),
                new Product("Hamburger",               ProductCategory.GASTRONOMIA, new BigDecimal("4.50"), true),
                new Product("Acqua naturale 500ml",    ProductCategory.BEVANDA,     new BigDecimal("1.00"), true),
                new Product("Acqua frizzante 500ml",   ProductCategory.BEVANDA,     new BigDecimal("1.00"), true),
                new Product("Coca Cola 330ml",         ProductCategory.BEVANDA,     new BigDecimal("2.00"), true),
                new Product("Birra media",             ProductCategory.BEVANDA,     new BigDecimal("3.50"), true),
                new Product("Vino rosso (calice)",     ProductCategory.BEVANDA,     new BigDecimal("2.50"), true),
                new Product("Succo di frutta",         ProductCategory.BEVANDA,     new BigDecimal("1.50"), true),
                new Product("Torta della nonna",       ProductCategory.DOLCE,       new BigDecimal("2.00"), true),
                new Product("Gelato artigianale",      ProductCategory.DOLCE,       new BigDecimal("2.50"), true),
                new Product("Zucchero filato",         ProductCategory.DOLCE,       new BigDecimal("1.00"), true),
                new Product("Patatine (sacchetto)",    ProductCategory.ALTRO,       new BigDecimal("1.50"), true)
        ));
    }

    private void seedEvents() {
        eventRepository.save(new Event("Sagra dell'Oratorio 2026", LocalDate.now(), EventStatus.APERTO));
    }
}
