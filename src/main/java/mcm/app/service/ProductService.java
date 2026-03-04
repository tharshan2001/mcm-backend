package mcm.app.service;

import mcm.app.dto.ProductRequest;
import mcm.app.entity.Product;
import mcm.app.entity.ProductCategory;
import mcm.app.entity.ProductImage;
import mcm.app.repository.ProductCategoryRepository;
import mcm.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    // --- Slug generation ---
    private String generateSlug(String name) {
        if (name == null) return null;
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\w\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }

    private String generateUniqueSlug(String name) {
        String slug = generateSlug(name);
        String originalSlug = slug;
        int counter = 1;
        while (productRepository.findBySlug(slug).isPresent()) {
            slug = originalSlug + "-" + counter++;
        }
        return slug;
    }

    // --- CRUD Operations ---
    public Product createProductFromRequest(ProductRequest request) {
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        product.setArchived(request.getArchived() != null ? request.getArchived() : false);
        product.setCategory(category);
        product.setSlug(generateUniqueSlug(request.getName()));

        // --- Add images safely ---
        Set<ProductImage> images = new HashSet<>();
        if (request.getImages() != null) {
            request.getImages().forEach(url -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setProduct(product);
                images.add(img);
            });
        }
        product.setImages(images);

        return productRepository.save(product);
    }

    public Product updateProductFromRequest(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // --- Update basic fields ---
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : product.getStockQuantity());
        product.setArchived(request.getArchived() != null ? request.getArchived() : product.getArchived());

        // --- Update slug if name changed ---
        if (!product.getName().equals(request.getName())) {
            product.setSlug(generateUniqueSlug(request.getName()));
        }

        // --- Update category ---
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // --- Update images safely ---
        // Clear existing images instead of replacing the collection
        product.getImages().clear();
        if (request.getImages() != null) {
            request.getImages().forEach(url -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setProduct(product);
                product.getImages().add(img);
            });
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    // Archive or unarchive a product
    public Product setArchived(Long productId, boolean archived) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setArchived(archived);
        return productRepository.save(product);
    }
}