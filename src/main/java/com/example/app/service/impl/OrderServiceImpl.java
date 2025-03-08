package com.example.app.service.impl;
import com.example.app.dto.CheckoutRequest;
import com.example.app.dto.DeliveryDTO;
import com.example.app.dto.OrderDTO;
import com.example.app.dto.OrderItemDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.PaymentDTO;
import com.example.app.entity.*;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.*;
import com.example.app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PromotionRepository promotionRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            PromotionRepository promotionRepository,
            DeliveryRepository deliveryRepository,
            PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.promotionRepository = promotionRepository;
        this.deliveryRepository = deliveryRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PagedResponse<OrderDTO> getAllOrders(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orders = orderRepository.findAll(pageable);

        List<OrderDTO> content = orders.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
    }

    @Override
    public OrderDTO getOrderById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return convertToDTO(order);
    }

    @Override
    public PagedResponse<OrderDTO> getOrdersByUser(Integer userId, int page, int size) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);

        List<OrderDTO> content = orders.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
    }

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        // Create new order
        Order order = new Order();

        // Set user if provided
        if (orderDTO.getUserId() != null) {
            User user = userRepository.findById(orderDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + orderDTO.getUserId()));
            order.setUser(user);
        } else {
            // Set guest info
            order.setGuestEmail(orderDTO.getGuestEmail());
            order.setGuestPhone(orderDTO.getGuestPhone());
            order.setGuestName(orderDTO.getGuestName());
        }

        order.setOrderStatus(Order.OrderStatus.valueOf(orderDTO.getOrderStatus()));
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setDiscountAmount(orderDTO.getDiscountAmount());
        order.setShippingFee(orderDTO.getShippingFee());
        order.setFinalAmount(orderDTO.getFinalAmount());
        order.setNote(orderDTO.getNote());

        Order savedOrder = orderRepository.save(order);

        // Process order items
        if (orderDTO.getItems() != null && !orderDTO.getItems().isEmpty()) {
            for (OrderItemDTO itemDTO : orderDTO.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);

                // Set product
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));
                orderItem.setProduct(product);

                // Set variant
                ProductVariant variant = variantRepository.findById(itemDTO.getVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + itemDTO.getVariantId()));
                orderItem.setVariant(variant);

                orderItem.setQuantity(itemDTO.getQuantity());
                orderItem.setUnitPrice(itemDTO.getUnitPrice());
                orderItem.setDiscount(itemDTO.getDiscount());
                orderItem.setTotal(itemDTO.getTotal());

                orderItemRepository.save(orderItem);

                // Update product stock
                int remainingStock = product.getStockQuantity() - itemDTO.getQuantity();
                product.setStockQuantity(Math.max(0, remainingStock));
                productRepository.save(product);

                // Update variant stock
                int remainingVariantStock = variant.getStockQuantity() - itemDTO.getQuantity();
                variant.setStockQuantity(Math.max(0, remainingVariantStock));
                if (remainingVariantStock <= 0) {
                    variant.setStatus(ProductVariant.VariantStatus.out_of_stock);
                }
                variantRepository.save(variant);
            }
        }

        return convertToDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderDTO processCheckout(CheckoutRequest checkoutRequest) {
        // Get cart
        Cart cart = cartRepository.findByIdWithItems(checkoutRequest.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + checkoutRequest.getCartId()));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Create new order
        Order order = new Order();

        // Set user if provided
        if (checkoutRequest.getUserId() != null) {
            User user = userRepository.findById(checkoutRequest.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + checkoutRequest.getUserId()));
            order.setUser(user);
        } else {
            // Set guest info
            order.setGuestEmail(checkoutRequest.getEmail());
            order.setGuestPhone(checkoutRequest.getPhone());
            order.setGuestName(checkoutRequest.getName());
        }

        order.setOrderStatus(Order.OrderStatus.pending);
        order.setNote(checkoutRequest.getNote());

        // Calculate order totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        // Apply promotion if provided
        if (checkoutRequest.getPromotionCode() != null && !checkoutRequest.getPromotionCode().isEmpty()) {
            Promotion promotion = promotionRepository.findByCode(checkoutRequest.getPromotionCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with code: " + checkoutRequest.getPromotionCode()));

            // Verify promotion is active
            LocalDateTime now = LocalDateTime.now();
            if (!promotion.getStatus().equals(Promotion.PromotionStatus.active) ||
                    now.isBefore(promotion.getStartDate()) ||
                    now.isAfter(promotion.getEndDate())) {
                throw new IllegalArgumentException("Promotion is not active");
            }

            // Verify usage limit not exceeded
            if (promotion.getUsageLimit() != null && promotion.getUsageCount() >= promotion.getUsageLimit()) {
                throw new IllegalArgumentException("Promotion usage limit exceeded");
            }

            // Calculate discount
            // Logic for calculating discount will depend on the discount type and business rules
            // This is a simplified example
            for (CartItem cartItem : cart.getItems()) {
                BigDecimal itemTotal = cartItem.getProduct().getBasePrice()
                        .add(cartItem.getVariant().getPriceAdjustment())
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }

            // Verify minimum order requirement
            if (totalAmount.compareTo(promotion.getMinimumOrder()) < 0) {
                throw new IllegalArgumentException("Order does not meet minimum amount for this promotion");
            }

            // Calculate discount based on type
            if (promotion.getDiscountType().equals(Promotion.DiscountType.percentage)) {
                discountAmount = totalAmount.multiply(promotion.getDiscountValue().divide(BigDecimal.valueOf(100)));
            } else {
                discountAmount = promotion.getDiscountValue();
                // Ensure discount doesn't exceed total
                if (discountAmount.compareTo(totalAmount) > 0) {
                    discountAmount = totalAmount;
                }
            }

            // Update promotion usage count
            promotion.setUsageCount(promotion.getUsageCount() + 1);
            promotionRepository.save(promotion);
        } else {
            // Calculate total without promotion
            for (CartItem cartItem : cart.getItems()) {
                BigDecimal itemTotal = cartItem.getProduct().getBasePrice()
                        .add(cartItem.getVariant().getPriceAdjustment())
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        // Set shipping fee (this could be calculated based on various factors)
        BigDecimal shippingFee = BigDecimal.valueOf(10.00); // Example fixed shipping fee

        // Calculate final amount
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).add(shippingFee);

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setFinalAmount(finalAmount);

        Order savedOrder = orderRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setQuantity(cartItem.getQuantity());

            // Calculate prices
            BigDecimal unitPrice = cartItem.getProduct().getBasePrice().add(cartItem.getVariant().getPriceAdjustment());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setDiscount(BigDecimal.ZERO); // Individual item discounts not implemented in this simplified example
            orderItem.setTotal(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            orderItemRepository.save(orderItem);

            // Update product stock
            Product product = cartItem.getProduct();
            int remainingStock = product.getStockQuantity() - cartItem.getQuantity();
            product.setStockQuantity(Math.max(0, remainingStock));
            productRepository.save(product);

            // Update variant stock
            ProductVariant variant = cartItem.getVariant();
            int remainingVariantStock = variant.getStockQuantity() - cartItem.getQuantity();
            variant.setStockQuantity(Math.max(0, remainingVariantStock));
            if (remainingVariantStock <= 0) {
                variant.setStatus(ProductVariant.VariantStatus.out_of_stock);
            }
            variantRepository.save(variant);
        }

        // Create delivery record
        Delivery delivery = new Delivery();
        delivery.setOrder(savedOrder);
        delivery.setShippingStatus(Delivery.ShippingStatus.pending);
        delivery.setShippingMethod(checkoutRequest.getShippingMethod());
        delivery.setShippingAddress(checkoutRequest.getShippingAddress());
        delivery.setContactPhone(checkoutRequest.getPhone());

        deliveryRepository.save(delivery);

        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setStatus(Payment.PaymentStatus.pending);
        payment.setAmount(finalAmount);

        // Handle payment method specific fields
        if (checkoutRequest.getPaymentMethod().equals("bank_transfer")) {
            payment.setBankAccount(checkoutRequest.getBankAccount());
            payment.setBankTransferCode(checkoutRequest.getBankTransferCode());
        }

        paymentRepository.save(payment);

        // Clear the cart after successful checkout
        cartItemRepository.deleteByCartId(cart.getId());

        return convertToDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Integer id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setOrderStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Update delivery status based on order status
        Optional<Delivery> deliveryOpt = deliveryRepository.findByOrderId(id);
        if (deliveryOpt.isPresent()) {
            Delivery delivery = deliveryOpt.get();

            if (status == Order.OrderStatus.shipped) {
                delivery.setShippingStatus(Delivery.ShippingStatus.shipped);
                delivery.setShippedDate(LocalDateTime.now());
                deliveryRepository.save(delivery);
            } else if (status == Order.OrderStatus.delivered) {
                delivery.setShippingStatus(Delivery.ShippingStatus.delivered);
                delivery.setDeliveredDate(LocalDateTime.now());
                deliveryRepository.save(delivery);
            }
        }

        // Update payment status if order is completed or cancelled
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            if (status == Order.OrderStatus.delivered) {
                payment.setStatus(Payment.PaymentStatus.completed);
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);
            } else if (status == Order.OrderStatus.cancelled) {
                payment.setStatus(Payment.PaymentStatus.failed);
                paymentRepository.save(payment);

                // If order is cancelled, restore stock
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
                for (OrderItem item : orderItems) {
                    // Restore product stock
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);

                    // Restore variant stock
                    ProductVariant variant = item.getVariant();
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                    if (variant.getStatus() == ProductVariant.VariantStatus.out_of_stock && variant.getStockQuantity() > 0) {
                        variant.setStatus(ProductVariant.VariantStatus.active);
                    }
                    variantRepository.save(variant);
                }
            }
        }

        return convertToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        // Check if order exists
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }

        // Delete order (cascading deletion should handle order items, delivery, and payment)
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatus(status);

        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateTotalSales(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalSales = orderRepository.calculateTotalSales(startDate, endDate);
        return totalSales != null ? totalSales : BigDecimal.ZERO;
    }

    @Override
    public Map<String, Long> getOrderStatusDistribution() {
        Map<String, Long> distribution = new HashMap<>();

        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            long count = orderRepository.findByOrderStatus(status).size();
            distribution.put(status.name(), count);
        }

        return distribution;
    }

    @Override
    public Long countOrders() {
        return orderRepository.count();
    }

    @Override
    public Long countPendingOrders() {
        return (long) orderRepository.findByOrderStatus(Order.OrderStatus.pending).size();
    }

    // Utility method to convert Entity to DTO
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());

        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUsername(order.getUser().getUsername());
        } else {
            dto.setGuestEmail(order.getGuestEmail());
            dto.setGuestPhone(order.getGuestPhone());
            dto.setGuestName(order.getGuestName());
        }

        dto.setOrderStatus(order.getOrderStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setNote(order.getNote());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Get order items
        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setOrderId(order.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            itemDTO.setProductImage(item.getProduct().getImage());
            itemDTO.setVariantId(item.getVariant().getId());
            itemDTO.setColor(item.getVariant().getColor());
            itemDTO.setSize(item.getVariant().getSize());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setUnitPrice(item.getUnitPrice());
            itemDTO.setDiscount(item.getDiscount());
            itemDTO.setTotal(item.getTotal());

            itemDTOs.add(itemDTO);
        }
        dto.setItems(itemDTOs);

        // Get delivery information if exists
        Optional<Delivery> deliveryOpt = deliveryRepository.findByOrderId(order.getId());
        if (deliveryOpt.isPresent()) {
            Delivery delivery = deliveryOpt.get();
            DeliveryDTO deliveryDTO = new DeliveryDTO();
            deliveryDTO.setId(delivery.getId());
            deliveryDTO.setOrderId(order.getId());
            deliveryDTO.setShippingStatus(delivery.getShippingStatus().name());
            deliveryDTO.setShippingMethod(delivery.getShippingMethod());
            deliveryDTO.setTrackingNumber(delivery.getTrackingNumber());
            deliveryDTO.setShippingAddress(delivery.getShippingAddress());
            deliveryDTO.setContactPhone(delivery.getContactPhone());
            deliveryDTO.setShippedDate(delivery.getShippedDate());
            deliveryDTO.setDeliveredDate(delivery.getDeliveredDate());
            deliveryDTO.setCreatedAt(delivery.getCreatedAt());
            deliveryDTO.setUpdatedAt(delivery.getUpdatedAt());

            dto.setDelivery(deliveryDTO);
        }

        // Get payment information if exists
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(order.getId());
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setId(payment.getId());
            paymentDTO.setOrderId(order.getId());
            paymentDTO.setStatus(payment.getStatus().name());
            paymentDTO.setAmount(payment.getAmount());
            paymentDTO.setBankTransferCode(payment.getBankTransferCode());
            paymentDTO.setBankAccount(payment.getBankAccount());
            paymentDTO.setPaymentDate(payment.getPaymentDate());
            paymentDTO.setCreatedAt(payment.getCreatedAt());

            dto.setPayment(paymentDTO);
        }

        return dto;
    }
    /**
     * Lấy danh sách đơn hàng của người dùng hiện tại dựa vào username

     */

    @Override
    public PagedResponse<OrderDTO> getOrdersByCurrentUser(String username, int page, int size) {
        // Tìm user từ username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // Lấy danh sách đơn hàng của user
        return getOrdersByUser(user.getId(), page, size);
    }



    // Cài đặt trong OrderServiceImpl
    @Override
    public Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        // Tính tổng doanh số
        BigDecimal totalSales = calculateTotalSales(startDate, endDate);
        stats.put("totalSales", totalSales);

        // Đếm tổng số đơn hàng
        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);
        stats.put("totalOrders", orders.size());

        // Tính doanh số theo trạng thái đơn hàng
        Map<String, BigDecimal> salesByStatus = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            BigDecimal statusTotal = orders.stream()
                    .filter(order -> order.getOrderStatus() == status)
                    .map(Order::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            salesByStatus.put(status.name(), statusTotal);
        }
        stats.put("salesByStatus", salesByStatus);

        // Tính doanh số theo ngày
        Map<String, BigDecimal> salesByDate = new HashMap<>();
        orders.forEach(order -> {
            String date = order.getCreatedAt().toLocalDate().toString();
            salesByDate.merge(date, order.getFinalAmount(), BigDecimal::add);
        });
        stats.put("salesByDate", salesByDate);

        // Tính giá trị đơn hàng trung bình
        BigDecimal averageOrderValue = totalSales.divide(
                BigDecimal.valueOf(Math.max(1, orders.size())),
                2, RoundingMode.HALF_UP);
        stats.put("averageOrderValue", averageOrderValue);

        return stats;
    }
}