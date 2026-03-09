package mcm.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mcm.app.dto.ProductRequest;
import mcm.app.dto.ProductResponse;
import mcm.app.entity.Product;
import mcm.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setArchived(product.getArchived());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        if (product.getImages() != null) {
            response.setImages(
                    product.getImages().stream()
                            .map(img -> img.getImageUrl())
                            .toList()
            );
        }
        return response;
    }

    // ---------------- PUBLIC ----------------
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(
                productService.getAllProducts().stream().map(this::mapToResponse).toList()
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(mapToResponse(product));
    }

    // ---------------- ADMIN ----------------
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("data") String requestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequest request = objectMapper.readValue(requestJson, ProductRequest.class);

        if (files != null) request.setFiles(files);

        Product product = productService.createProductFromRequest(request);
        return ResponseEntity.ok(mapToResponse(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestPart("data") String requestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequest request = objectMapper.readValue(requestJson, ProductRequest.class);

        if (files != null) request.setFiles(files);

        Product updated = productService.updateProductFromRequest(id, request);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/archive")
    public ResponseEntity<ProductResponse> archiveProduct(
            @PathVariable Long id,
            @RequestParam boolean archived
    ) {
        Product product = productService.setArchived(id, archived);
        return ResponseEntity.ok(mapToResponse(product));
    }

    // Infinite scroll API using DTO
    @GetMapping("/scroll")
    public ResponseEntity<List<ProductResponse>> getProductsForScroll(
            @RequestParam(required = false) Long cursor
    ) {
        List<Product> products = productService.getProductsForInfiniteScroll(cursor);

        List<ProductResponse> responseList = products.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }


}