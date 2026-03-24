-- =====================================================
-- SAMPLE DATA FOR MCM E-COMMERCE APPLICATION
-- Works with existing data (uses INSERT IGNORE)
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- ROLES (2 records) - Skip if exists
-- =====================================================
INSERT IGNORE INTO roles (id, name) VALUES 
(1, 'ADMIN'),
(2, 'CUSTOMER');

-- =====================================================
-- PRODUCT CATEGORIES (10 categories) - Skip if exists
-- =====================================================
INSERT IGNORE INTO product_category (id, name, description) VALUES
(1, 'Shirts', 'Classic and modern shirts for all occasions'),
(2, 'Pants', 'Formal and casual pants'),
(3, 'Dresses', 'Elegant dresses for women'),
(4, 'Jackets', 'Winter and summer jackets'),
(5, 'Shoes', 'Casual and formal footwear'),
(6, 'Accessories', 'Bags, belts, and more'),
(7, 'Skirts', 'Various skirt styles'),
(8, 'T-Shirts', 'Casual t-shirts'),
(9, 'Shorts', 'Summer shorts'),
(10, 'Sweaters', 'Warm sweaters for winter');

-- =====================================================
-- PRODUCTS (100 products)
-- =====================================================
INSERT IGNORE INTO product (id, name, slug, description, price, stock_quantity, archived, category_id) VALUES
(1, 'Classic White Shirt', 'classic-white-shirt', 'Elegant white cotton shirt', 49.99, 50, 0, 1),
(2, 'Blue Oxford Shirt', 'blue-oxford-shirt', 'Casual blue oxford shirt', 59.99, 45, 0, 1),
(3, 'Black Formal Pants', 'black-formal-pants', 'Professional black pants', 69.99, 30, 0, 2),
(4, 'Navy Chinos', 'navy-chinos', 'Comfortable navy chinos', 59.99, 40, 0, 2),
(5, 'Evening Gown', 'evening-gown', 'Elegant evening gown', 199.99, 15, 0, 3),
(6, 'Cocktail Dress', 'cocktail-dress', 'Short cocktail dress', 129.99, 25, 0, 3),
(7, 'Winter Jacket', 'winter-jacket', 'Warm winter jacket', 149.99, 20, 0, 4),
(8, 'Denim Jacket', 'denim-jacket', 'Classic denim jacket', 89.99, 35, 0, 4),
(9, 'Leather Shoes', 'leather-shoes', 'Formal leather shoes', 119.99, 25, 0, 5),
(10, 'Casual Sneakers', 'casual-sneakers', 'Comfortable casual sneakers', 79.99, 50, 0, 5);

-- Generate products 11-100
INSERT INTO product (name, slug, description, price, stock_quantity, archived, category_id)
SELECT 
    CONCAT('Product ', n, ' - ', 
        CASE (n % 10)
            WHEN 0 THEN 'Basic Edition'
            WHEN 1 THEN 'Premium'
            WHEN 2 THEN 'Classic'
            WHEN 3 THEN 'Modern'
            WHEN 4 THEN 'Elegant'
            WHEN 5 THEN 'Casual'
            WHEN 6 THEN 'Vintage'
            WHEN 7 THEN 'Urban'
            WHEN 8 THEN 'Luxury'
            WHEN 9 THEN 'Standard'
        END
    ) as name,
    CONCAT('product-', n) as slug,
    CONCAT('Description for product ', n) as description,
    ROUND(20 + (RAND() * 180), 2) as price,
    FLOOR(10 + (RAND() * 90)) as stock_quantity,
    0 as archived,
    ((n-1) % 10) + 1 as category_id
FROM (
    SELECT 11 + (a.N + b.N * 10) as n
    FROM 
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8) b
) numbers
WHERE n <= 100;

-- =====================================================
-- PRODUCT IMAGES (3 images per product)
-- =====================================================
INSERT INTO product_image (image_url, product_id)
SELECT 
    CONCAT('https://mcm-bucket.s3.amazonaws.com/products/', p.id, '/image', i, '.jpg'),
    p.id
FROM product p
CROSS JOIN (SELECT 1 as i UNION SELECT 2 UNION SELECT 3) img_count;

-- =====================================================
-- COUPONS (10 coupons)
-- =====================================================
INSERT IGNORE INTO coupons (id, code, discount_type, discount_value, min_order_amount, max_usage, used_count, expiry_date, active, description) VALUES
(1, 'SUMMER20', 'PERCENTAGE', 20.00, 50.00, 100, 15, '2026-12-31 23:59:59', 1, '20% off summer sale'),
(2, 'WELCOME10', 'PERCENTAGE', 10.00, 30.00, 200, 45, '2026-12-31 23:59:59', 1, '10% off for new users'),
(3, 'FLAT50', 'FIXED', 50.00, 100.00, 50, 8, '2026-06-30 23:59:59', 1, '$50 off orders over $100'),
(4, 'SAVE15', 'PERCENTAGE', 15.00, 40.00, 150, 30, '2026-12-31 23:59:59', 1, '15% off your order'),
(5, 'FREESHIP', 'FIXED', 5.00, 75.00, 500, 120, '2026-12-31 23:59:59', 1, 'Free shipping on orders over $75'),
(6, 'VIP25', 'PERCENTAGE', 25.00, 150.00, 30, 5, '2026-09-30 23:59:59', 1, '25% off for VIP customers'),
(7, 'FLASH50', 'FIXED', 50.00, 80.00, 20, 3, '2026-04-30 23:59:59', 1, 'Flash sale - $50 off'),
(8, 'NEWYEAR30', 'PERCENTAGE', 30.00, 60.00, 75, 25, '2026-01-31 23:59:59', 0, 'New Year special - expired'),
(9, 'LOYALTY15', 'PERCENTAGE', 15.00, 35.00, 100, 40, '2026-12-31 23:59:59', 1, 'Loyalty reward'),
(10, 'BUNDLE10', 'PERCENTAGE', 10.00, 25.00, 300, 90, '2026-12-31 23:59:59', 1, '10% off bundle purchases');

-- =====================================================
-- USERS (100 + 1 admin) - Skip existing users
-- =====================================================
INSERT INTO users (full_name, email, phone_number, password)
SELECT 
    CONCAT('Customer ', n) as full_name,
    CONCAT('customer', n, '@email.com') as email,
    CONCAT('+1234567', LPAD(n, 4, '0')) as phone_number,
    '$2a$10$8k/wE3yQRXJXJJMQ.qwWe.3O5HJqJQZ8HNyNVdVJfYPmQrX5rPQW' as password
FROM (
    SELECT 11 + (a.N + b.N * 10) as n
    FROM 
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8) b
) numbers
WHERE n <= 100;

-- =====================================================
-- USER ROLES - Assign CUSTOMER role to new users
-- =====================================================
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, 2 
FROM users u 
WHERE u.id > 1;

-- =====================================================
-- ADDRESSES (2 per user for users 2-100)
-- =====================================================
INSERT INTO addresses (user_id, full_name, phone, country, state, city, postal_code, address_line, is_default)
SELECT 
    u.id as user_id,
    u.full_name,
    u.phone_number,
    'United States' as country,
    CASE (u.id % 5)
        WHEN 0 THEN 'California'
        WHEN 1 THEN 'New York'
        WHEN 2 THEN 'Texas'
        WHEN 3 THEN 'Florida'
        WHEN 4 THEN 'Illinois'
    END as state,
    CASE (u.id % 3)
        WHEN 0 THEN 'Los Angeles'
        WHEN 1 THEN 'New York City'
        WHEN 2 THEN 'Houston'
    END as city,
    CONCAT('100', LPAD(u.id, 4, '0')) as postal_code,
    CONCAT(FLOOR(RAND() * 9000) + 1000, ' ', 
        CASE (u.id % 5)
            WHEN 0 THEN 'Oak Street'
            WHEN 1 THEN 'Main Avenue'
            WHEN 2 THEN 'Park Boulevard'
            WHEN 3 THEN 'Maple Drive'
            WHEN 4 THEN 'Cedar Lane'
        END
    ) as address_line,
    CASE WHEN u.id = 2 THEN 1 ELSE 0 END as is_default
FROM users u
WHERE u.id > 1;

-- Second address for each user
INSERT INTO addresses (user_id, full_name, phone, country, state, city, postal_code, address_line, is_default)
SELECT 
    u.id as user_id,
    CONCAT(u.full_name, ' (Office)') as full_name,
    u.phone_number,
    'United States' as country,
    CASE (u.id % 5)
        WHEN 1 THEN 'California'
        WHEN 2 THEN 'New York'
        WHEN 3 THEN 'Texas'
        WHEN 4 THEN 'Florida'
        WHEN 0 THEN 'Illinois'
    END as state,
    CASE (u.id % 3)
        WHEN 1 THEN 'San Francisco'
        WHEN 2 THEN 'Chicago'
        WHEN 0 THEN 'Dallas'
    END as city,
    CONCAT('200', LPAD(u.id, 4, '0')) as postal_code,
    CONCAT(FLOOR(RAND() * 9000) + 1000, ' Business Park Ave') as address_line,
    0 as is_default
FROM users u
WHERE u.id > 1;

-- =====================================================
-- CARTS (one per user)
-- =====================================================
INSERT IGNORE INTO cart (user_id, total_price)
SELECT id, 0 FROM users;

-- =====================================================
-- CART ITEMS (random items per cart)
-- =====================================================
INSERT INTO cart_item (cart_id, product_id, quantity, price)
SELECT 
    ci.cart_id,
    ci.product_id,
    ci.quantity,
    (SELECT price FROM product WHERE id = ci.product_id) as price
FROM (
    SELECT 
        (u.id) as cart_id,
        ((u.id * 7 + i.n) % 100) + 1 as product_id,
        ((u.id + i.n) % 5) + 1 as quantity
    FROM users u
    CROSS JOIN (SELECT 1 as n UNION SELECT 2 UNION SELECT 3) i
    WHERE u.id <= 10
) ci;

-- Update cart totals
UPDATE cart c SET total_price = (
    SELECT COALESCE(SUM(ci.quantity * ci.price), 0) 
    FROM cart_item ci 
    WHERE ci.cart_id = c.id
);

-- =====================================================
-- ORDERS (100 orders over last 60 days)
-- =====================================================
INSERT INTO orders (user_id, order_date, order_status, total_price, payment_status, shipping_address, tracking_number, coupon_id, discount_amount)
SELECT 
    ((n % 99) + 2) as user_id,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY) as order_date,
    CASE (n % 4)
        WHEN 0 THEN 'PLACED'
        WHEN 1 THEN 'SHIPPED'
        WHEN 2 THEN 'DELIVERED'
        WHEN 3 THEN 'PLACED'
    END as order_status,
    ROUND(50 + (RAND() * 250), 2) as total_price,
    CASE (n % 3)
        WHEN 0 THEN 'PAID'
        WHEN 1 THEN 'PAID'
        WHEN 2 THEN 'PENDING'
    END as payment_status,
    CONCAT(FLOOR(RAND() * 9000) + 1000, ' Main Street, New York, NY') as shipping_address,
    CONCAT('TRK', LPAD(n, 8, '0')) as tracking_number,
    CASE WHEN n % 5 = 0 THEN ((n % 10) + 1) ELSE NULL END as coupon_id,
    CASE WHEN n % 5 = 0 THEN ROUND(ROUND(50 + (RAND() * 250), 2) * 0.15, 2) ELSE 0 END as discount_amount
FROM (
    SELECT 1 + (a.N + b.N * 10) as n
    FROM 
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
    LIMIT 100
) numbers;

-- =====================================================
-- ORDER ITEMS (2-5 items per order)
-- =====================================================
INSERT INTO order_item (order_id, product_id, quantity, price)
SELECT 
    ((oi.n - 1) % 100) + 1 as order_id,
    ((oi.n * 3 + 7) % 100) + 1 as product_id,
    ((oi.n % 3) + 1) as quantity,
    (SELECT price FROM product WHERE id = ((oi.n * 3 + 7) % 100) + 1) as price
FROM (
    SELECT (a.N + b.N * 10 + c.N * 100) + 1 as n
    FROM 
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) a,
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b,
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) c
    WHERE (a.N + b.N * 10 + c.N * 100) < 300
) oi;

-- =====================================================
-- PAYMENTS (one per order)
-- =====================================================
INSERT IGNORE INTO payment (user_id, amount, currency, stripe_payment_id, status, created_at)
SELECT 
    o.user_id as user_id,
    o.total_price as amount,
    'usd' as currency,
    CONCAT('pi_', LPAD(o.id, 16, '0')) as stripe_payment_id,
    o.payment_status as status,
    o.order_date as created_at
FROM orders o;

-- =====================================================
-- FEEDBACK (50 feedbacks)
-- =====================================================
INSERT INTO feedback (product_id, user_id, rating, comments, created_at)
SELECT 
    ((n * 3 + 7) % 100) + 1 as product_id,
    ((n % 99) + 2) as user_id,
    ((n % 5) + 1) as rating,
    CASE (n % 5)
        WHEN 0 THEN 'Great product! Highly recommended.'
        WHEN 1 THEN 'Good quality, fast shipping.'
        WHEN 2 THEN 'Average product, expected better.'
        WHEN 3 THEN 'Excellent! Will buy again.'
        WHEN 4 THEN 'Decent product, worth the price.'
    END as comments,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as created_at
FROM (
    SELECT 1 + (a.N + b.N * 10) as n
    FROM 
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
        (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) b
    LIMIT 50
) numbers;

SET FOREIGN_KEY_CHECKS = 1;
