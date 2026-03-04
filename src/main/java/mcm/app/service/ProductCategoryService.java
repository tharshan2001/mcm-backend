package mcm.app.service;

import mcm.app.entity.ProductCategory;
import mcm.app.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {

    @Autowired
    private ProductCategoryRepository repository;

    // Create or update category
    public ProductCategory saveCategory(ProductCategory category) {
        return repository.save(category);
    }

    // Get category by ID
    public ProductCategory getCategoryById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // Get all categories
    public List<ProductCategory> getAllCategories() {
        return repository.findAll();
    }

    // Delete category
    public void deleteCategory(Long id) {
        ProductCategory category = getCategoryById(id);
        repository.delete(category);
    }

    // Optional: find by name
    public ProductCategory getCategoryByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}