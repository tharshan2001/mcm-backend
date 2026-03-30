package mcm.app.service;

import mcm.app.entity.Feedback;
import mcm.app.entity.Product;
import mcm.app.entity.User;
import mcm.app.repository.FeedbackRepository;
import mcm.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ProductRepository productRepository;

    // Submit feedback using User object directly
    public Feedback submitFeedback(User user, Long productId, int rating, String comments) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setProduct(product);
        feedback.setRating(rating);
        feedback.setComments(comments);
        feedback.setCreatedAt(LocalDateTime.now());

        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }


    public List<Feedback> getFeedbackByProductWithPagination(Long productId, Long cursor, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit); // wrap limit into Pageable

        if (cursor == null) {
            // First page: latest feedbacks
            return feedbackRepository.findByProductIdOrderByIdDesc(productId, pageRequest);
        } else {
            // Next page: feedbacks with ID < cursor
            return feedbackRepository.findByProductIdAndIdLessThanOrderByIdDesc(productId, cursor, pageRequest);
        }
    }

    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
    }

    public Feedback updateFeedback(Long feedbackId, Long userId, int rating, String comments) {
        Feedback feedback = getFeedbackById(feedbackId);

        if (!feedback.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own feedback");
        }

        feedback.setRating(rating);
        feedback.setComments(comments);
        return feedbackRepository.save(feedback);
    }

    public void deleteFeedback(Long feedbackId, Long userId) {
        Feedback feedback = getFeedbackById(feedbackId);

        if (!feedback.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own feedback");
        }

        feedbackRepository.delete(feedback);
    }
}