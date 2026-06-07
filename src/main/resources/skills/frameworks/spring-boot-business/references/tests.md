# Testing Patterns — Business Reference

## Unit Test — Service Layer
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ProductServiceImpl productService;

    @Test
    void create_shouldPersistAndReturnProduct() {
        var request = new CreateProductRequest("SKU-001", "Laptop", new BigDecimal("2999.99"));
        var saved = buildProduct(1L, "SKU-001", "Laptop");
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productRepository.save(any())).thenReturn(saved);

        ProductResponse response = productService.create(request);

        assertThat(response.sku()).isEqualTo("SKU-001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_shouldThrowWhenSkuAlreadyExists() {
        var request = new CreateProductRequest("SKU-DUP", "Dup", BigDecimal.ONE);
        when(productRepository.existsBySku("SKU-DUP")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
            .isInstanceOf(DuplicateSkuException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        var id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
            .isInstanceOf(ProductNotFoundException.class);
    }
}
```

## Integration Test — Controller Layer (MockMvc)
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void POST_products_shouldReturn201() throws Exception {
        var request = new CreateProductRequest("SKU-IT-001", "Test Product", new BigDecimal("99.99"));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sku").value("SKU-IT-001"))
            .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @WithMockUser
    void GET_products_id_shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void POST_products_shouldReturn422OnValidationFailure() throws Exception {
        var invalid = new CreateProductRequest("", "Name", new BigDecimal("-1"));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldErrors.sku").exists());
    }
}
```

## Testcontainers (real PostgreSQL in tests)
```java
@SpringBootTest
@Testcontainers
class ProductRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private ProductRepository productRepository;

    @Test
    void shouldPersistAndRetrieveProduct() {
        var product = buildProduct(null, "SKU-TC-001", "Persisted Product");
        var saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findById(saved.getId())).isPresent();
    }
}
```

### pom.xml additions for Testcontainers
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```
