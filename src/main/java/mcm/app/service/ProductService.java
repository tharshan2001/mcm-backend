package mcm.app.service;

import mcm.app.dto.ProductRequest;
import mcm.app.entity.Product;
import mcm.app.entity.ProductCategory;
import mcm.app.entity.ProductImage;
import mcm.app.repository.ProductCategoryRepository;
import mcm.app.repository.ProductRepository;
import mcm.app.utils.S3Utils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final S3Utils s3Utils;

    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          S3Utils s3Utils) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.s3Utils = s3Utils;
    }

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

    @Transactional
    public Product createProductFromRequest(ProductRequest request) throws IOException {
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

        // Add images
        if (request.getFiles() != null) {
            List<String> uploadedUrls = s3Utils.uploadProductImages(request.getFiles());
            uploadedUrls.forEach(url -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setProduct(product);           // owning side
                product.getImages().add(img);      // inverse side
            });
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProductFromRequest(Long productId, ProductRequest request) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getName().equals(request.getName())) {
            product.setSlug(generateUniqueSlug(request.getName()));
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : product.getStockQuantity());
        product.setArchived(request.getArchived() != null ? request.getArchived() : product.getArchived());

        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Clear old images and upload new ones
        product.getImages().clear();
        if (request.getFiles() != null) {
            List<String> uploadedUrls = s3Utils.uploadProductImages(request.getFiles());
            uploadedUrls.forEach(url -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setProduct(product);
                product.getImages().add(img);
            });
        }

        return productRepository.save(product);
    }

    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    public List<Product> getAllProducts() {
        return productRepository.findByArchivedFalse();
    }

    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    public Product setArchived(Long productId, boolean archived) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setArchived(archived);
        return productRepository.save(product);
    }


    public List<Product> getProductsForInfiniteScroll(Long cursorId) {
        if (cursorId == null) {
            // First page
            return productRepository.findTop10ByOrderByIdAsc();
        }
        // Subsequent pages: get next 10 products after cursor
        return productRepository.findTop10ByIdGreaterThanOrderByIdAsc(cursorId);
    }

    public List<Product> getRandomRelatedProducts(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Product> relatedProducts = productRepository.findRandomRelatedProducts(
                product.getCategory().getId(), productId, 6
        );

        return relatedProducts;
    }

    public List<Product> getRelatedProductsByCategoryAndName(String categoryName, String name, int limit) {
        if (name != null && !name.isEmpty()) {
            return productRepository.findRelatedByCategoryNameAndName(categoryName, name, limit);
        }
        return productRepository.findRelatedByCategoryName(categoryName, limit);
    }

    public List<Product> getTrendingProducts(int limit) {
        List<Product> trending = productRepository.findTrendingProducts(limit);
        if (trending == null || trending.isEmpty()) {
            return productRepository.findRandomProducts(limit);
        }
        return trending;
    }

    public List<Product> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, 
            BigDecimal maxPrice, Integer minStock, Integer maxStock, String sortBy) {
        List<Product> products = productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice, minStock, maxStock);
        
        if (sortBy == null) return products;
        
        return switch (sortBy) {
            case "price_asc" -> products.stream().sorted(Comparator.comparing(Product::getPrice)).toList();
            case "price_desc" -> products.stream().sorted(Comparator.comparing(Product::getPrice).reversed()).toList();
            case "name_asc" -> products.stream().sorted(Comparator.comparing(Product::getName)).toList();
            case "name_desc" -> products.stream().sorted(Comparator.comparing(Product::getName).reversed()).toList();
            case "newest" -> products.stream().sorted(Comparator.comparing(Product::getId).reversed()).toList();
            default -> products;
        };
    }
}