package mcm.app.controller;

import mcm.app.entity.Feedback;
import mcm.app.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // User submits feedback
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/submit")
    public ResponseEntity<Feedback> submitFeedback(@RequestParam Long userId,
                                                   @RequestParam Long productId,
                                                   @RequestParam int rating,
                                                   @RequestParam(required=false) String comments) {
        Feedback feedback = feedbackService.submitFeedback(userId, productId, rating, comments);
        return ResponseEntity.ok(feedback);
    }

    // Admin views all feedback
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    // Optionally, get feedback by product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Feedback>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByProduct(productId));
    }
}