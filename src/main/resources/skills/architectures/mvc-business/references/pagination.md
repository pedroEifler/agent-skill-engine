# Pagination & Search — Business Reference

## Repository with Search

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Product> findByActiveTrue(Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true
          AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """)
    Page<Product> searchProducts(@Param("search") String search,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  Pageable pageable);
}
```

## Controller with Full Filter Support

```java
@GetMapping
public String list(Model model,
                   @RequestParam(defaultValue = "0")  int page,
                   @RequestParam(defaultValue = "10") int size,
                   @RequestParam(defaultValue = "name") String sort,
                   @RequestParam(defaultValue = "asc") String dir,
                   @RequestParam(required = false) String search,
                   @RequestParam(required = false) BigDecimal minPrice,
                   @RequestParam(required = false) BigDecimal maxPrice) {

    var direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    var pageable = PageRequest.of(page, size, Sort.by(direction, sort));
    var products = productService.search(search, minPrice, maxPrice, pageable);

    model.addAttribute("products", products);
    model.addAttribute("search", search);
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("sort", sort);
    model.addAttribute("dir", dir);
    model.addAttribute("reversedDir", "asc".equals(dir) ? "desc" : "asc");
    return "product/list";
}
```

## Sortable Column Headers in Template

```html
<th>
    <a th:href="@{/products(page=0, sort='name', dir=${sort == 'name' ? reversedDir : 'asc'}, search=${search})}">
        Name
        <i th:if="${sort == 'name'}"
           th:classappend="${dir == 'asc'} ? 'bi-sort-up' : 'bi-sort-down'"
           class="bi"></i>
    </a>
</th>
```
