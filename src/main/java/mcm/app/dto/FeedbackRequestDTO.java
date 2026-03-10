package mcm.app.dto;

import lombok.Data;

@Data
public class FeedbackRequestDTO {
    private Long productId;
    private int rating;
    private String comments;
}