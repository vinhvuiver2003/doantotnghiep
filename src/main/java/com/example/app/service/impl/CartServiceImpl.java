package com.example.app.service.impl;

import com.example.app.dto.CartDTO;
import com.example.app.dto.CartItemDTO;
import com.example.app.entity.Cart;
import com.example.app.entity.CartItem;
import com.example.app.entity.Product;
import com.example.app.entity.ProductImage;
import com.example.app.entity.ProductVariant;
import com.example.app.entity.User;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.CartItemRepository;
import com.example.app.repository.CartRepository;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.ProductVariantRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Autowired
    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public CartDTO getCartById(Integer id) {
        Cart cart = cartRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));

        return convertToDTO(cart);
    }

    @Override
    public CartDTO getCartByUserId(Integer userId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Find user's cart or create a new one
        Optional<Cart> optionalCart = cartRepository.findByUserIdWithFullDetails(userId);

        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            // Kiểm tra nếu giỏ hàng đã được thanh toán, tạo giỏ hàng mới
            if (cart.getIsCheckedOut()) {
                return createCart(userId, null);
            }
            return convertToDTO(cart);
        } else {
            // Create new cart for user
            return createCart(userId, null);
        }
    }

    @Override
    public CartDTO getCartBySessionId(String sessionId) {
        // Find cart by session ID or create a new one
        Optional<Cart> optionalCart = cartRepository.findBySessionIdWithFullDetails(sessionId);

        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            // Kiểm tra nếu giỏ hàng đã được thanh toán, tạo giỏ hàng mới
            if (cart.getIsCheckedOut()) {
                return createCart(null, sessionId);
            }
            return convertToDTO(cart);
        } else {
            // Create new cart for guest
            return createCart(null, sessionId);
        }
    }

    @Override
    @Transactional
    public CartDTO createCart(Integer userId, String sessionId) {
        Cart cart = new Cart();

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            cart.setUser(user);
        } else if (sessionId != null) {
            cart.setSessionId(sessionId);
        } else {
            throw new IllegalArgumentException("Either userId or sessionId must be provided");
        }

        Cart savedCart = cartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    @Override
    @Transactional
    public CartDTO addItemToCart(Integer cartId, CartItemDTO cartItemDTO) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        Product product = productRepository.findByIdWithImagesAndDefaultVariant(cartItemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + cartItemDTO.getProductId()));

        ProductVariant variant = variantRepository.findByIdWithImages(cartItemDTO.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + cartItemDTO.getVariantId()));

        // Check if product is active
        if (product.getStatus() != Product.ProductStatus.active) {
            throw new IllegalArgumentException("Product is not available");
        }

        // Check if variant is active and in stock
        if (variant.getStatus() != ProductVariant.VariantStatus.active) {
            throw new IllegalArgumentException("Variant is not available");
        }

        if (variant.getStockQuantity() < cartItemDTO.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
                cartId, cartItemDTO.getProductId(), cartItemDTO.getVariantId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setVariant(variant);
            newItem.setQuantity(cartItemDTO.getQuantity());

            cartItemRepository.save(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Refresh cart data
        Cart updatedCart = cartRepository.findByIdWithFullDetails(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        return convertToDTO(updatedCart);
    }

    @Override
    @Transactional
    public CartDTO updateCartItem(Integer cartId, Integer cartItemId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Verify cart item belongs to the cart
        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }

        // Validate quantity
        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            cartItemRepository.delete(cartItem);
        } else {
            // Check stock
            ProductVariant variant = cartItem.getVariant();
            if (variant.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("Not enough stock available");
            }

            // Update quantity
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Refresh cart data
        Cart updatedCart = cartRepository.findByIdWithFullDetails(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        return convertToDTO(updatedCart);
    }

    @Override
    @Transactional
    public CartDTO removeItemFromCart(Integer cartId, Integer cartItemId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Verify cart item belongs to the cart
        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }

        // Remove item
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Refresh cart data
        Cart updatedCart = cartRepository.findByIdWithFullDetails(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        return convertToDTO(updatedCart);
    }

    @Override
    @Transactional
    public void clearCart(Integer cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        // Delete all items in the cart
        cartItemRepository.deleteByCartId(cartId);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void deleteExpiredCarts(int expirationDays) {
        LocalDateTime expirationDate = LocalDateTime.now().minusDays(expirationDays);
        List<Cart> expiredCarts = cartRepository.findExpiredGuestCarts(expirationDate);

        for (Cart cart : expiredCarts) {
            // Delete all items in the cart
            cartItemRepository.deleteByCartId(cart.getId());

            // Delete the cart
            cartRepository.delete(cart);
        }
    }

    @Override
    @Transactional
    public void mergeGuestCartWithUserCart(String sessionId, Integer userId) {
        // Find guest cart
        Optional<Cart> optionalGuestCart = cartRepository.findBySessionId(sessionId);
        if (!optionalGuestCart.isPresent()) {
            return; // No guest cart to merge
        }

        // Find or create user cart
        Optional<Cart> optionalUserCart = cartRepository.findByUserId(userId);
        Cart userCart;

        if (optionalUserCart.isPresent()) {
            userCart = optionalUserCart.get();
            // Kiểm tra nếu giỏ hàng người dùng đã thanh toán, tạo giỏ hàng mới
            if (userCart.getIsCheckedOut()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
                userCart = new Cart();
                userCart.setUser(user);
                userCart = cartRepository.save(userCart);
            }
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            userCart = new Cart();
            userCart.setUser(user);
            userCart = cartRepository.save(userCart);
        }

        Cart guestCart = optionalGuestCart.get();
        
        // Kiểm tra nếu giỏ hàng khách đã thanh toán, bỏ qua việc hợp nhất
        if (guestCart.getIsCheckedOut()) {
            return;
        }

        // Merge items
        List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());

        for (CartItem guestItem : guestItems) {
            // Check if item already exists in user cart
            Optional<CartItem> existingUserItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
                    userCart.getId(), guestItem.getProduct().getId(), guestItem.getVariant().getId());

            if (existingUserItem.isPresent()) {
                // Update quantity
                CartItem userItem = existingUserItem.get();
                userItem.setQuantity(userItem.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(userItem);
            } else {
                // Create new item in user cart
                CartItem newUserItem = new CartItem();
                newUserItem.setCart(userCart);
                newUserItem.setProduct(guestItem.getProduct());
                newUserItem.setVariant(guestItem.getVariant());
                newUserItem.setQuantity(guestItem.getQuantity());

                cartItemRepository.save(newUserItem);
            }
        }

        // Update user cart timestamp
        userCart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(userCart);

        // Delete guest cart and its items
        cartItemRepository.deleteByCartId(guestCart.getId());
        cartRepository.delete(guestCart);
    }

    @Override
    public CartDTO getCartByCurrentUser(String username) {
        // Tìm user từ username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // Tìm giỏ hàng của user hoặc tạo mới
        Optional<Cart> optionalCart = cartRepository.findByUserId(user.getId());

        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            // Kiểm tra nếu giỏ hàng đã được thanh toán, tạo giỏ hàng mới
            if (cart.getIsCheckedOut()) {
                return createCart(user.getId(), null);
            }
            return convertToDTO(cart);
        } else {
            // Tạo giỏ hàng mới cho user
            return createCart(user.getId(), null);
        }
    }

    @Override
    @Transactional
    public CartDTO mergeGuestCartWithUserCart(String sessionId, String username) {
        // Tìm user từ username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // Tìm giỏ hàng khách từ sessionId
        Optional<Cart> optionalGuestCart = cartRepository.findBySessionId(sessionId);
        if (!optionalGuestCart.isPresent()) {
            throw new ResourceNotFoundException("Guest cart not found with sessionId: " + sessionId);
        }

        Cart guestCart = optionalGuestCart.get();
        // Kiểm tra nếu giỏ hàng khách đã thanh toán, bỏ qua việc hợp nhất
        if (guestCart.getIsCheckedOut()) {
            // Trả về giỏ hàng hiện tại của người dùng hoặc tạo mới
            return getCartByCurrentUser(username);
        }

        // Tìm hoặc tạo giỏ hàng người dùng
        Optional<Cart> optionalUserCart = cartRepository.findByUserId(user.getId());
        Cart userCart;

        if (optionalUserCart.isPresent()) {
            userCart = optionalUserCart.get();
            // Kiểm tra nếu giỏ hàng người dùng đã thanh toán, tạo giỏ hàng mới
            if (userCart.getIsCheckedOut()) {
                userCart = new Cart();
                userCart.setUser(user);
                userCart = cartRepository.save(userCart);
            }
        } else {
            userCart = new Cart();
            userCart.setUser(user);
            userCart = cartRepository.save(userCart);
        }

        // Hợp nhất các mục trong giỏ hàng
        List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());

        for (CartItem guestItem : guestItems) {
            // Kiểm tra xem sản phẩm đã tồn tại trong giỏ hàng người dùng chưa
            Optional<CartItem> existingUserItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
                    userCart.getId(), guestItem.getProduct().getId(), guestItem.getVariant().getId());

            if (existingUserItem.isPresent()) {
                // Cập nhật số lượng
                CartItem userItem = existingUserItem.get();
                userItem.setQuantity(userItem.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(userItem);
            } else {
                // Tạo mục mới trong giỏ hàng người dùng
                CartItem newUserItem = new CartItem();
                newUserItem.setCart(userCart);
                newUserItem.setProduct(guestItem.getProduct());
                newUserItem.setVariant(guestItem.getVariant());
                newUserItem.setQuantity(guestItem.getQuantity());

                cartItemRepository.save(newUserItem);
            }
        }

        // Cập nhật thời gian cho giỏ hàng người dùng
        userCart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(userCart);

        // Xóa giỏ hàng khách và các mục trong đó
        cartItemRepository.deleteByCartId(guestCart.getId());
        cartRepository.delete(guestCart);

        // Lấy giỏ hàng người dùng đã cập nhật
        Cart updatedUserCart = cartRepository.findByIdWithItems(userCart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User cart not found after merging"));

        return convertToDTO(updatedUserCart);
    }

    // Utility method to convert Entity to DTO
    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());

        if (cart.getUser() != null) {
            dto.setUserId(cart.getUser().getId());
            dto.setUsername(cart.getUser().getUsername());
        }

        dto.setSessionId(cart.getSessionId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        // Get cart items
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            CartItemDTO itemDTO = new CartItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setCartId(cart.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            
            // Sử dụng phương thức tiện ích để lấy ảnh đại diện
            itemDTO.setProductImage(item.getProduct().getMainImageUrl());
            
            itemDTO.setVariantId(item.getVariant().getId());
            itemDTO.setColor(item.getVariant().getColor());
            itemDTO.setSize(item.getVariant().getSize());
            itemDTO.setQuantity(item.getQuantity());

            // Calculate prices
            BigDecimal unitPrice = item.getProduct().getBasePrice().add(item.getVariant().getPriceAdjustment());
            itemDTO.setUnitPrice(unitPrice);

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            itemDTO.setTotal(itemTotal);

            totalAmount = totalAmount.add(itemTotal);

            itemDTO.setAddedAt(item.getAddedAt());
            itemDTO.setUpdatedAt(item.getUpdatedAt());

            itemDTOs.add(itemDTO);
        }

        dto.setItems(itemDTOs);
        dto.setTotalAmount(totalAmount);
        dto.setTotalItems(itemDTOs.size());

        return dto;
    }
}