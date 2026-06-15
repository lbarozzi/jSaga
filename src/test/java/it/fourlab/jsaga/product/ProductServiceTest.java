package it.fourlab.jsaga.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findAllWithActiveFilterReturnsFilteredResults() {
        Product product = new Product("Acqua", ProductCategory.BEVANDA, new BigDecimal("1.50"), true);
        when(productRepository.findByActive(true)).thenReturn(List.of(product));

        List<ProductResponse> results = productService.findAll(true);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Acqua");
        assertThat(results.getFirst().category()).isEqualTo(ProductCategory.BEVANDA);
        verify(productRepository).findByActive(true);
    }
}
