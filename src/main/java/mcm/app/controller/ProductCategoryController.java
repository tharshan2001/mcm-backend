package mcm.app.controller;

import mcm.app.dto.ProductCategoryResponseDTO;
import mcm.app.entity.ProductCategory;
import mcm.app.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService service;

    // Convert Entity -> DTO
    private ProductCategoryResponseDTO mapToDTO(ProductCategory category){
        return new ProductCategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    // Get all categories
    @GetMapping
    public ResponseEntity<List<ProductCategoryResponseDTO>> getAllCategories() {
        List<ProductCategoryResponseDTO> categories = service.getAllCategories()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categories);
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryResponseDTO> getCategory(@PathVariable Long id) {
        ProductCategory category = service.getCategoryById(id);
        return ResponseEntity.ok(mapToDTO(category));
    }

    // Admin creates a category
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductCategoryResponseDTO> createCategory(@RequestBody ProductCategory category) {
        ProductCategory saved = service.saveCategory(category);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    // Admin updates a category
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryResponseDTO> updateCategory(@PathVariable Long id,
                                                                     @RequestBody ProductCategory updatedCategory) {
        ProductCategory category = service.getCategoryById(id);
        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());

        ProductCategory saved = service.saveCategory(category);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    // Admin deletes a category
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        service.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }


}