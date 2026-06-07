# Testing Spring MVC — Business Reference

## Unit Test — Controller with @WebMvcTest

```java
@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private ProductService productService;

    @Test
    @WithMockUser(roles = "USER")
    void list_shouldRenderListView() throws Exception {
        var page = new PageImpl<>(List.of(
            new Product("Laptop", new BigDecimal("1500")),
            new Product("Mouse", new BigDecimal("25"))
        ));
        when(productService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/products"))
            .andExpect(status().isOk())
            .andExpect(view().name("product/list"))
            .andExpect(model().attributeExists("products"))
            .andExpect(model().attribute("products", hasProperty("totalElements", is(2L))));
    }

    @Test
    @WithMockUser(roles = "USER")
    void save_withValidForm_shouldRedirect() throws Exception {
        mockMvc.perform(post("/products")
                .param("name", "Keyboard")
                .param("price", "99.99")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/products"))
            .andExpect(flash().attribute("successMessage", notNullValue()));

        verify(productService).save(any(Product.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void save_withBlankName_shouldReturnFormWithErrors() throws Exception {
        mockMvc.perform(post("/products")
                .param("name", "")
                .param("price", "99.99")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("product/form"))
            .andExpect(model().attributeHasFieldErrors("form", "name"));

        verify(productService, never()).save(any());
    }

    @Test
    void list_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/products"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoint_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/users"))
            .andExpect(status().isForbidden());
    }
}
```

## Integration Test — Full Stack

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;

    @Test
    @WithMockUser(roles = "USER")
    void fullCrudFlow() throws Exception {
        // Create
        mockMvc.perform(post("/products")
                .param("name", "Integration Product")
                .param("price", "42.00")
                .with(csrf()))
            .andExpect(redirectedUrl("/products"));

        assertThat(productRepository.findAll()).extracting("name")
            .contains("Integration Product");

        // Read
        mockMvc.perform(get("/products"))
            .andExpect(content().string(containsString("Integration Product")));
    }
}
```

## Page Object Pattern for Readable Tests

```java
class ProductListPage {

    private final MvcResult result;

    ProductListPage(MvcResult result) { this.result = result; }

    static ProductListPage visit(MockMvc mockMvc) throws Exception {
        var result = mockMvc.perform(get("/products").with(user("user").roles("USER")))
            .andExpect(status().isOk())
            .andReturn();
        return new ProductListPage(result);
    }

    boolean contains(String productName) {
        return result.getResponse().getContentAsString().contains(productName);
    }

    int totalProducts() {
        // parse the total from model attribute
        var model = (Page<?>) result.getModelAndView().getModel().get("products");
        return (int) model.getTotalElements();
    }
}

// Usage:
@Test
void list_shouldShowAllActiveProducts() throws Exception {
    productRepository.save(new Product("Active Product", BigDecimal.TEN));
    var page = ProductListPage.visit(mockMvc);
    assertThat(page.contains("Active Product")).isTrue();
}
```
