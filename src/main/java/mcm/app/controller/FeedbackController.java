package mcm.app.controller;

import mcm.app.dto.FeedbackRequestDTO;
import mcm.app.dto.FeedbackResponseDTO;
import mcm.app.entity.Feedback;
import mcm.app.entity.User;
import mcm.app.security.CustomUserDetails;
import mcm.app.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // User submits feedback
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponseDTO> submitFeedback(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody FeedbackRequestDTO request) {

        User user = principal.getUser();

        Feedback feedback = feedbackService.submitFeedback(
                user,
                request.getProductId(),
                request.getRating(),
                request.getComments()
        );

        FeedbackResponseDTO response = mapToDTO(feedback);
        return ResponseEntity.ok(response);
    }

    // Admin views all feedback
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<FeedbackResponseDTO>> getAllFeedback() {
        List<Feedback> feedbackList = feedbackService.getAllFeedback();
        List<FeedbackResponseDTO> response = feedbackList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Get feedback by product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<FeedbackResponseDTO>> getByProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<Feedback> feedbackList = feedbackService.getFeedbackByProductWithPagination(productId, cursor, limit);

        List<FeedbackResponseDTO> response = feedbackList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Customer edits their own feedback
    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponseDTO> updateFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody FeedbackRequestDTO request) {

        User user = principal.getUser();

        Feedback feedback = feedbackService.updateFeedback(
                id,
                user.getId(),
                request.getRating(),
                request.getComments()
        );

        return ResponseEntity.ok(mapToDTO(feedback));
    }

    // Customer deletes their own feedback
    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        User user = principal.getUser();
        feedbackService.deleteFeedback(id, user.getId());

        return ResponseEntity.ok("Feedback deleted successfully");
    }

    // Mapper method
    private FeedbackResponseDTO mapToDTO(Feedback feedback) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.setId(feedback.getId());
        dto.setProductId(feedback.getProduct().getId());
        dto.setProductName(feedback.getProduct().getName());
        dto.setRating(feedback.getRating());
        dto.setComments(feedback.getComments());
        dto.setUsername(feedback.getUser().getFullName());
        dto.setCreatedAt(feedback.getCreatedAt());
        return dto;
    }
}