package com.example.app.service.impl;
import com.example.app.dto.CheckoutRequest;
import com.example.app.dto.DeliveryDTO;
import com.example.app.dto.OrderDTO;
import com.example.app.dto.OrderItemDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.PaymentDTO;
import com.example.app.entity.*;
import com.example.app.entity.Cart;
import com.example.app.entity.CartItem;
import com.example.app.entity.Delivery;
import com.example.app.entity.Order;
import com.example.app.entity.OrderItem;
import com.example.app.entity.Payment;
import com.example.app.entity.Product;
import com.example.app.entity.ProductImage;
import com.example.app.entity.ProductVariant;
import com.example.app.entity.Promotion;
import com.example.app.entity.User;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.*;
import com.example.app.service.EmailService;
import com.example.app.service.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Value("${frontend.url}")
    String FRONTEND_URL;
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
    private final ModelMapper modelMapper;
    private final EmailService emailService;

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
            PaymentRepository paymentRepository,
            ModelMapper modelMapper,
            EmailService emailService) {
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
        this.modelMapper = modelMapper;
        this.emailService = emailService;
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
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return convertToDTO(order);
    }

    @Override
    public PagedResponse<OrderDTO> getOrdersByUser(Integer userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findByUserIdWithDetails(userId, pageable);

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
        // Kiểm tra người dùng
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + orderDTO.getUserId()));

        Order order = modelMapper.map(orderDTO, Order.class);
        order.setUser(user);

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        Order savedOrder = orderRepository.save(order);

        sendOrderConfirmationEmail(savedOrder);

        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderDTO processCheckout(CheckoutRequest checkoutRequest) {
        // Get cart
        Cart cart = cartRepository.findByIdWithFullDetails(checkoutRequest.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + checkoutRequest.getCartId()));

        if (cart.getIsCheckedOut()) {
            throw new IllegalStateException("Giỏ hàng này đã được thanh toán. Vui lòng tạo giỏ hàng mới.");
        }

        if (checkoutRequest.getUserId() == null) {
            throw new IllegalArgumentException("Bạn cần đăng nhập để thực hiện thanh toán");
        }

        // Get user (bắt buộc)
        User user = userRepository.findById(checkoutRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + checkoutRequest.getUserId()));

        Order order = new Order();

        order.setUser(user);
        order.setOrderStatus(Order.OrderStatus.pending);
        order.setNote(checkoutRequest.getNote());

        // Calculate order totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (checkoutRequest.getPromotionCode() != null && !checkoutRequest.getPromotionCode().isEmpty()) {
            Promotion promotion = promotionRepository.findByCode(checkoutRequest.getPromotionCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with code: " + checkoutRequest.getPromotionCode()));

            LocalDateTime now = LocalDateTime.now();
            if (!promotion.getStatus().equals(Promotion.PromotionStatus.active) ||
                    now.isBefore(promotion.getStartDate()) ||
                    now.isAfter(promotion.getEndDate())) {
                throw new IllegalArgumentException("Promotion is not active");
            }

            if (promotion.getUsageLimit() != null && promotion.getUsageCount() >= promotion.getUsageLimit()) {
                throw new IllegalArgumentException("Promotion usage limit exceeded");
            }

            for (CartItem cartItem : cart.getItems()) {
                BigDecimal itemTotal = cartItem.getProduct().getBasePrice()
                        .add(cartItem.getVariant().getPriceAdjustment())
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }

            if (totalAmount.compareTo(promotion.getMinimumOrder()) < 0) {
                throw new IllegalArgumentException("Order does not meet minimum amount for this promotion");
            }

            if (promotion.getDiscountType().equals(Promotion.DiscountType.percentage)) {
                discountAmount = totalAmount.multiply(promotion.getDiscountValue().divide(BigDecimal.valueOf(100)));
            } else {
                discountAmount = promotion.getDiscountValue();
                // Ensure discount doesn't exceed total
                if (discountAmount.compareTo(totalAmount) > 0) {
                    discountAmount = totalAmount;
                }
            }

            promotion.setUsageCount(promotion.getUsageCount() + 1);
            promotionRepository.save(promotion);
        } else {
            for (CartItem cartItem : cart.getItems()) {
                BigDecimal itemTotal = cartItem.getProduct().getBasePrice()
                        .add(cartItem.getVariant().getPriceAdjustment())
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        BigDecimal shippingFee;
        if (checkoutRequest.getShippingMethod().equals("express")) {
            shippingFee = BigDecimal.valueOf(50000);
        } else {
            // Standard shipping
            shippingFee = BigDecimal.valueOf(20000);
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount).add(shippingFee);

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setFinalAmount(finalAmount);

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal unitPrice = cartItem.getProduct().getBasePrice().add(cartItem.getVariant().getPriceAdjustment());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setDiscount(BigDecimal.ZERO); // Individual item discounts not implemented in this simplified example
            orderItem.setTotal(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            orderItemRepository.save(orderItem);

            ProductVariant variant = cartItem.getVariant();
            int remainingVariantStock = variant.getStockQuantity() - cartItem.getQuantity();
            variant.setStockQuantity(Math.max(0, remainingVariantStock));
            if (remainingVariantStock <= 0) {
                variant.setStatus(ProductVariant.VariantStatus.out_of_stock);
            }
            variantRepository.save(variant);
        }

        Delivery delivery = new Delivery();
        delivery.setOrder(savedOrder);
        delivery.setShippingStatus(Delivery.ShippingStatus.pending);
        delivery.setShippingMethod(checkoutRequest.getShippingMethod());
        delivery.setShippingAddress(checkoutRequest.getShippingAddress());
        delivery.setContactPhone(checkoutRequest.getPhone());

        deliveryRepository.save(delivery);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setStatus(Payment.PaymentStatus.pending);
        payment.setAmount(finalAmount);

        if (checkoutRequest.getPaymentMethod().equals("bank_transfer")) {
            payment.setBankAccount(checkoutRequest.getBankAccount());
            payment.setBankTransferCode(checkoutRequest.getBankTransferCode());
        }

        paymentRepository.save(payment);

        cart.setIsCheckedOut(true);
        cartRepository.save(cart);

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

                List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
                for (OrderItem item : orderItems) {
                    ProductVariant variant = item.getVariant();
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                    if (variant.getStatus() == ProductVariant.VariantStatus.out_of_stock && variant.getStockQuantity() > 0) {
                        variant.setStatus(ProductVariant.VariantStatus.active);
                    }
                    variantRepository.save(variant);
                }
            }
        }

        sendOrderStatusUpdateEmail(updatedOrder, order.getOrderStatus().name());

        return convertToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }

        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatusWithDetails(status);

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

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());

        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUsername(order.getUser().getUsername());
            dto.setUser(order.getUser());
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

        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setOrderId(order.getId());
                
                if (item.getProduct() != null) {
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setProductImage(item.getProduct().getMainImageUrl());
                }
                
                // Chỉ lấy thông tin cần thiết từ variant để tránh vòng lặp sâu
                if (item.getVariant() != null) {
                    itemDTO.setVariantId(item.getVariant().getId());
                    itemDTO.setColor(item.getVariant().getColor());
                    itemDTO.setSize(item.getVariant().getSize());
                }
                
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setUnitPrice(item.getUnitPrice());
                itemDTO.setDiscount(item.getDiscount());
                itemDTO.setTotal(item.getTotal());

                itemDTOs.add(itemDTO);
            }
        }
        dto.setItems(itemDTOs);

        if (order.getDelivery() != null) {
            DeliveryDTO deliveryDTO = new DeliveryDTO();
            Delivery delivery = order.getDelivery();
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
        } else {
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
        }

        if (order.getPayment() != null) {
            PaymentDTO paymentDTO = new PaymentDTO();
            Payment payment = order.getPayment();
            paymentDTO.setId(payment.getId());
            paymentDTO.setOrderId(order.getId());
            paymentDTO.setStatus(payment.getStatus().name());
            paymentDTO.setAmount(payment.getAmount());
            paymentDTO.setBankTransferCode(payment.getBankTransferCode());
            paymentDTO.setBankAccount(payment.getBankAccount());
            paymentDTO.setPaymentDate(payment.getPaymentDate());
            paymentDTO.setCreatedAt(payment.getCreatedAt());

            dto.setPayment(paymentDTO);
        } else {
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
        }

        return dto;
    }


    @Override
    public PagedResponse<OrderDTO> getOrdersByCurrentUser(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return getOrdersByUser(user.getId(), page, size);
    }



    @Override
    public Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        BigDecimal totalSales = calculateTotalSales(startDate, endDate);
        stats.put("totalSales", totalSales);

        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);
        stats.put("totalOrders", orders.size());

        Map<String, BigDecimal> salesByStatus = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            BigDecimal statusTotal = orders.stream()
                    .filter(order -> order.getOrderStatus() == status)
                    .map(Order::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            salesByStatus.put(status.name(), statusTotal);
        }
        stats.put("salesByStatus", salesByStatus);

        Map<String, BigDecimal> salesByDate = new HashMap<>();
        orders.forEach(order -> {
            String date = order.getCreatedAt().toLocalDate().toString();
            salesByDate.merge(date, order.getFinalAmount(), BigDecimal::add);
        });
        stats.put("salesByDate", salesByDate);

        BigDecimal averageOrderValue = totalSales.divide(
                BigDecimal.valueOf(Math.max(1, orders.size())),
                2, RoundingMode.HALF_UP);
        stats.put("averageOrderValue", averageOrderValue);

        return stats;
    }

    private void sendOrderConfirmationEmail(Order order) {
        Map<String, Object> orderDetails = new HashMap<>();

        orderDetails.put("orderId", order.getId());
        orderDetails.put("orderDate", order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        orderDetails.put("customerName", order.getUser().getFirstName() + " " + order.getUser().getLastName());

        String paymentMethod = "Thanh toán khi nhận hàng";
        if (order.getPayment() != null) {
            paymentMethod = order.getPayment().getStatus().name();
        }
        orderDetails.put("paymentMethod", paymentMethod);

        String shippingAddress = "";
        if (order.getDelivery() != null) {
            shippingAddress = order.getDelivery().getShippingAddress();
        }
        orderDetails.put("shippingAddress", shippingAddress);

        List<Map<String, Object>> items = order.getItems().stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getProduct().getName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("price", formatCurrency(item.getUnitPrice().doubleValue()));
            itemMap.put("subtotal", formatCurrency(item.getTotal().doubleValue()));
            return itemMap;
        }).collect(Collectors.toList());

        orderDetails.put("items", items);

        orderDetails.put("subtotal", formatCurrency(order.getTotalAmount().doubleValue()));
        orderDetails.put("shippingFee", formatCurrency(order.getShippingFee().doubleValue()));
        orderDetails.put("discount", formatCurrency(order.getDiscountAmount().doubleValue()));
        orderDetails.put("total", formatCurrency(order.getFinalAmount().doubleValue()));

        orderDetails.put("orderLink", FRONTEND_URL+"/account/orders/" + order.getId());

        // Gửi emails
        emailService.sendOrderConfirmationEmail(order.getUser().getEmail(), orderDetails);
    }

     private void sendOrderStatusUpdateEmail(Order order, String oldStatus) {
        if (oldStatus.equals(order.getOrderStatus().name())) {
            return;
        }

        Map<String, Object> statusDetails = new HashMap<>();

        statusDetails.put("orderId", order.getId());
        statusDetails.put("orderDate", order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        statusDetails.put("customerName", order.getUser().getFirstName() + " " + order.getUser().getLastName());

        statusDetails.put("statusMessage", getStatusMessage(order.getOrderStatus().name()));
        statusDetails.put("statusClass", getStatusClass(order.getOrderStatus().name()));

        if ("SHIPPED".equals(order.getOrderStatus().name()) && order.getDelivery() != null) {
            Map<String, String> trackingInfo = new HashMap<>();
            Delivery delivery = order.getDelivery();
            trackingInfo.put("carrier", delivery.getShippingMethod());
            trackingInfo.put("trackingNumber", delivery.getTrackingNumber() != null ? delivery.getTrackingNumber() : "");
            trackingInfo.put("estimatedDelivery", delivery.getDeliveredDate() != null ?
                    delivery.getDeliveredDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Đang cập nhật");

            statusDetails.put("trackingInfo", trackingInfo);
        }

        if ("CANCELLED".equals(order.getOrderStatus().name()) && order.getNote() != null) {
            statusDetails.put("cancellationReason", order.getNote());
        }

        statusDetails.put("orderLink", FRONTEND_URL+"/account/orders/" + order.getId());
        emailService.sendOrderStatusUpdateEmail(order.getUser().getEmail(), statusDetails);
    }

    private double calculateSubtotal(Order order) {
        return order.getItems().stream()
                .mapToDouble(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue())
                .sum();
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f₫", amount);
    }

    private String getStatusMessage(String status) {
        switch (status) {
            case "PENDING": return "Đơn hàng đang chờ xử lý";
            case "PROCESSING": return "Đơn hàng đang được xử lý";
            case "SHIPPED": return "Đơn hàng đã được giao cho đơn vị vận chuyển";
            case "DELIVERED": return "Đơn hàng đã được giao thành công";
            case "CANCELLED": return "Đơn hàng đã bị hủy";
            default: return "Trạng thái đơn hàng đã được cập nhật";
        }
    }

    private String getStatusClass(String status) {
        switch (status) {
            case "PENDING":
            case "PROCESSING": return "status-processing";
            case "SHIPPED": return "status-shipped";
            case "DELIVERED": return "status-delivered";
            case "CANCELLED": return "status-cancelled";
            default: return "";
        }
    }

    @Override
    @Transactional
    public OrderDTO confirmOrderDelivery(Integer orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + orderId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với username: " + username));

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xác nhận đơn hàng này");
        }

        if (order.getOrderStatus() != Order.OrderStatus.shipped) {
            throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng có trạng thái 'đang giao hàng'");
        }

        order.setOrderStatus(Order.OrderStatus.delivered);

        if (order.getDelivery() != null) {
            order.getDelivery().setShippingStatus(Delivery.ShippingStatus.delivered);
            order.getDelivery().setDeliveredDate(LocalDateTime.now());
        } else {
            Optional<Delivery> deliveryOpt = deliveryRepository.findByOrderId(order.getId());
            if (deliveryOpt.isPresent()) {
                Delivery delivery = deliveryOpt.get();
                delivery.setShippingStatus(Delivery.ShippingStatus.delivered);
                delivery.setDeliveredDate(LocalDateTime.now());
                deliveryRepository.save(delivery);
            }
        }

        Order updatedOrder = orderRepository.save(order);

        sendOrderStatusUpdateEmail(updatedOrder, Order.OrderStatus.shipped.name());

        return convertToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrderByUser(Integer orderId, String username, String cancelReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + orderId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với username: " + username));

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getOrderStatus() != Order.OrderStatus.pending) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng có trạng thái 'chờ xác nhận'");
        }

        if (cancelReason != null && !cancelReason.trim().isEmpty()) {
            order.setNote("Đơn hàng đã bị hủy bởi người dùng. Lý do: " + cancelReason);
        } else {
            order.setNote("Đơn hàng đã bị hủy bởi người dùng");
        }

        String oldStatus = order.getOrderStatus().name();
        order.setOrderStatus(Order.OrderStatus.cancelled);

        if (order.getPayment() != null) {
            order.getPayment().setStatus(Payment.PaymentStatus.failed);
        } else {
            Optional<Payment> paymentOpt = paymentRepository.findByOrderId(order.getId());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus(Payment.PaymentStatus.failed);
                paymentRepository.save(payment);
            }
        }

        for (OrderItem item : order.getItems()) {

            ProductVariant variant = item.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            if (variant.getStatus() == ProductVariant.VariantStatus.out_of_stock && variant.getStockQuantity() > 0) {
                variant.setStatus(ProductVariant.VariantStatus.active);
            }
            variantRepository.save(variant);
        }

        Order updatedOrder = orderRepository.save(order);

        sendOrderStatusUpdateEmail(updatedOrder, oldStatus);

        return convertToDTO(updatedOrder);
    }
}