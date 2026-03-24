package mcm.app.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    private String keyword;
    private Long categoryId;
    private String categoryName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minStock;
    private Integer maxStock;
    private String sortBy; // price_asc, price_desc, name_asc, name_desc, newest
    private Integer page;
    private Integer size;
}
