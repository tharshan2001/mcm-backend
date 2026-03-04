package mcm.app.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import mcm.app.entity.Payment;
import mcm.app.entity.User;
import mcm.app.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Create a Stripe PaymentIntent for a user and save a pending Payment record.
     *
     * @param user   The user making the payment
     * @param amount The amount to charge (in USD)
     * @return Stripe PaymentIntent
     * @throws Exception Stripe API errors
     */
    public PaymentIntent createPaymentIntent(User user, BigDecimal amount) throws Exception {
        // Set Stripe secret key
        Stripe.apiKey = stripeApiKey;

        // Build PaymentIntent parameters
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // Stripe expects cents
                .setCurrency("usd")
                .addPaymentMethodType("card") // correct method for v31.x
                .build();

        // Create PaymentIntent in Stripe
        PaymentIntent intent = PaymentIntent.create(params);

        // Save a pending Payment in DB
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setStripePaymentId(intent.getId());
        payment.setStatus("PENDING"); // initial status
        paymentRepository.save(payment);

        return intent;
    }

    /**
     * Mark a payment as SUCCESS
     *
     * @param paymentIntentId Stripe PaymentIntent ID
     * @return Updated Payment entity
     */
    public Payment markPaymentSuccess(String paymentIntentId) {
        Payment payment = paymentRepository.findAll()
                .stream()
                .filter(p -> p.getStripePaymentId().equals(paymentIntentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus("SUCCESS");
        return paymentRepository.save(payment);
    }

    /**
     * Mark a payment as FAILED
     *
     * @param paymentIntentId Stripe PaymentIntent ID
     * @return Updated Payment entity
     */
    public Payment markPaymentFailed(String paymentIntentId) {
        Payment payment = paymentRepository.findAll()
                .stream()
                .filter(p -> p.getStripePaymentId().equals(paymentIntentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus("FAILED");
        return paymentRepository.save(payment);
    }
}