package mcm.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private int rating;
    private String comments;
    private String username;
    private LocalDateTime createdAt;
}