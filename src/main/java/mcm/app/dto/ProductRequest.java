package mcm.app.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating or updating a product.
 * Supports only file uploads to S3.
 */
@Getter
@Setter
public class ProductRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean archived;
    private Long categoryId;

    // List of new files to upload to S3
    private List<MultipartFile> files;
}