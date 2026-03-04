package mcm.app.controller;

import mcm.app.dto.ProductRequest;
import mcm.app.dto.ProductResponse;
import mcm.app.entity.Product;
import mcm.app.entity.ProductImage;
import mcm.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        Set<ProductImage> images = product.getImages();
        if (images != null) {
            response.setImages(images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));
        }
        return response;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        List<ProductResponse> response = productService.getAllProducts()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(mapToResponse(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request) {
        Product product = productService.createProductFromRequest(request);
        return ResponseEntity.ok(mapToResponse(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        Product updatedProduct = productService.updateProductFromRequest(id, request);
        return ResponseEntity.ok(mapToResponse(updatedProduct));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // Archive or unarchive a product
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/archive")
    public ResponseEntity<ProductResponse> archiveProduct(@PathVariable Long id,
                                                          @RequestParam boolean archived) {
        Product product = productService.setArchived(id, archived);
        // Map to response manually since we don't use mappingService
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setArchived(product.getArchived());
        response.setSlug(product.getSlug());
        response.setCategoryId(product.getCategory().getId());
        response.setImages(product.getImages().stream()
                .map(img -> img.getImageUrl())
                .toList());

        return ResponseEntity.ok(response);
    }
}