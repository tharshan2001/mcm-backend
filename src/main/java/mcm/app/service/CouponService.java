package mcm.app.service;

import mcm.app.dto.CouponRequest;
import mcm.app.dto.CouponResponse;
import mcm.app.entity.Coupon;
import mcm.app.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAllByOrderByIdDesc();
    }

    public List<Coupon> getActiveCoupons() {
        return couponRepository.findAll().stream()
                .filter(c -> c.isActive() && (c.getExpiryDate() == null || c.getExpiryDate().isAfter(LocalDateTime.now())))
                .toList();
    }

    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }

    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }

    @Transactional
    public Coupon createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new RuntimeException("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxUsage(request.getMaxUsage());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(request.isActive());
        coupon.setDescription(request.getDescription());
        coupon.setUsedCount(0);

        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = getCouponById(id);

        if (!coupon.getCode().equals(request.getCode().toUpperCase())) {
            if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
                throw new RuntimeException("Coupon code already exists");
            }
            coupon.setCode(request.getCode().toUpperCase());
        }

        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxUsage(request.getMaxUsage());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(request.isActive());
        coupon.setDescription(request.getDescription());

        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = getCouponById(id);
        couponRepository.delete(coupon);
    }

    public boolean isCouponValid(Coupon coupon, BigDecimal orderAmount) {
        if (!coupon.isActive()) {
            return false;
        }

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (coupon.getMaxUsage() != null && coupon.getUsedCount() >= coupon.getMaxUsage()) {
            return false;
        }

        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            return false;
        }

        return true;
    }

    @Transactional
    public void incrementUsage(Long couponId) {
        Coupon coupon = getCouponById(couponId);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }
}