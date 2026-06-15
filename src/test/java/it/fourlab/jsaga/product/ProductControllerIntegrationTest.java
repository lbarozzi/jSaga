package it.fourlab.jsaga.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductControllerIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
        void createAndListProduct() {
        ProductRequest request = new ProductRequest(
            "Panino Salamella",
            ProductCategory.GASTRONOMIA,
            new BigDecimal("6.50"),
            true);

        ProductResponse created = productService.create(request);
        List<ProductResponse> products = productService.findAll(null);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Panino Salamella");
        assertThat(products).hasSize(1);
        assertThat(products.getFirst().category()).isEqualTo(ProductCategory.GASTRONOMIA);
    }
}
