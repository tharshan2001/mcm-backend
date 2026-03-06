package mcm.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for returning product data in API responses.
 */
@Getter
@Setter
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean archived;
    private Long categoryId;
    private String categoryName;
    private List<String> images;
}