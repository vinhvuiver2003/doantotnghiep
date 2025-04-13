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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        org.springframework.data.domain.PageRequest pageRequest =
            org.springframework.data.domain.PageRequest.of(0, 1);
        List<Cart> userCarts = cartRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageRequest);

        if (!userCarts.isEmpty()) {
            Cart cart = userCarts.get(0);
            if (cart.getIsCheckedOut()) {
                return createCart(userId, null);
            }
            return convertToDTO(cart);
        } else {
            return createCart(userId, null);
        }
    }

    @Override
    public CartDTO getCartBySessionId(String sessionId) {
        Optional<Cart> optionalCart = cartRepository.findBySessionIdWithFullDetails(sessionId);

        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            if (cart.getIsCheckedOut()) {
                return createCart(null, sessionId);
            }
            return convertToDTO(cart);
        } else {
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

        if (product.getStatus() != Product.ProductStatus.active) {
            throw new IllegalArgumentException("Product is not available");
        }

        if (variant.getStatus() != ProductVariant.VariantStatus.active) {
            throw new IllegalArgumentException("Variant is not available");
        }

        if (variant.getStockQuantity() < cartItemDTO.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
                cartId, cartItemDTO.getProductId(), cartItemDTO.getVariantId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setVariant(variant);
            newItem.setQuantity(cartItemDTO.getQuantity());

            cartItemRepository.save(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

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

        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            ProductVariant variant = cartItem.getVariant();
            if (variant.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("Not enough stock available");
            }

            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

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

        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }

        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        Cart updatedCart = cartRepository.findByIdWithFullDetails(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        return convertToDTO(updatedCart);
    }

    @Override
    @Transactional
    public void clearCart(Integer cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

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
            cartItemRepository.deleteByCartId(cart.getId());

            // Delete the cart
            cartRepository.delete(cart);
        }
    }

    @Override
    @Transactional
    public void mergeGuestCartWithUserCart(String sessionId, Integer userId) {
        Optional<Cart> optionalGuestCart = cartRepository.findBySessionId(sessionId);
        if (!optionalGuestCart.isPresent()) {
            return; // No guest cart to merge
        }

        org.springframework.data.domain.PageRequest pageRequest =
            org.springframework.data.domain.PageRequest.of(0, 1);
        List<Cart> userCarts = cartRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageRequest);
        Cart userCart;

        if (!userCarts.isEmpty()) {
            userCart = userCarts.get(0);
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
        
        if (guestCart.getIsCheckedOut()) {
            return;
        }

        List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());

        for (CartItem guestItem : guestItems) {
            Optional<CartItem> existingUserItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
                    userCart.getId(), guestItem.getProduct().getId(), guestItem.getVariant().getId());

            if (existingUserItem.isPresent()) {
                CartItem userItem = existingUserItem.get();
                userItem.setQuantity(userItem.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(userItem);
            } else {
                CartItem newUserItem = new CartItem();
                newUserItem.setCart(userCart);
                newUserItem.setProduct(guestItem.getProduct());
                newUserItem.setVariant(guestItem.getVariant());
                newUserItem.setQuantity(guestItem.getQuantity());

                cartItemRepository.save(newUserItem);
            }
        }

        userCart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(userCart);

        cartItemRepository.deleteByCartId(guestCart.getId());
        cartRepository.delete(guestCart);
    }

    @Override
    public CartDTO getCartByCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        org.springframework.data.domain.PageRequest pageRequest =
            org.springframework.data.domain.PageRequest.of(0, 1);
        List<Cart> userCarts = cartRepository.findByUserIdOrderByUpdatedAtDesc(user.getId(), pageRequest);

        if (!userCarts.isEmpty()) {
            Cart cart = userCarts.get(0);
            if (cart.getIsCheckedOut()) {
                return createCart(user.getId(), null);
            }
            return convertToDTO(cart);
        } else {
            return createCart(user.getId(), null);
        }
    }

    @Override
    @Transactional
    public CartDTO mergeGuestCartWithUserCart(String sessionId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Optional<Cart> optionalGuestCart = cartRepository.findBySessionId(sessionId);
        if (!optionalGuestCart.isPresent()) {
            throw new ResourceNotFoundException("Guest cart not found with sessionId: " + sessionId);
        }

        Cart guestCart = optionalGuestCart.get();
        if (guestCart.getIsCheckedOut()) {
            return getCartByCurrentUser(username);
        }

        org.springframework.data.domain.PageRequest pageRequest =
            org.springframework.data.domain.PageRequest.of(0, 1);
        List<Cart> userCarts = cartRepository.findByUserIdOrderByUpdatedAtDesc(user.getId(), pageRequest);
        Cart userCart;

        if (!userCarts.isEmpty()) {
            userCart = userCarts.get(0);
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

        List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());

        for (CartItem guestItem : guestItems) {
            Optional<CartItem> existingUserItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
                    userCart.getId(), guestItem.getProduct().getId(), guestItem.getVariant().getId());

            if (existingUserItem.isPresent()) {
                CartItem userItem = existingUserItem.get();
                userItem.setQuantity(userItem.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(userItem);
            } else {
                CartItem newUserItem = new CartItem();
                newUserItem.setCart(userCart);
                newUserItem.setProduct(guestItem.getProduct());
                newUserItem.setVariant(guestItem.getVariant());
                newUserItem.setQuantity(guestItem.getQuantity());

                cartItemRepository.save(newUserItem);
            }
        }

        userCart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(userCart);

        cartItemRepository.deleteByCartId(guestCart.getId());
        cartRepository.delete(guestCart);

        Cart updatedUserCart = cartRepository.findByIdWithItems(userCart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User cart not found after merging"));

        return convertToDTO(updatedUserCart);
    }

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

        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            CartItemDTO itemDTO = new CartItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setCartId(cart.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            
            itemDTO.setProductImage(item.getProduct().getMainImageUrl());
            
            itemDTO.setVariantId(item.getVariant().getId());
            itemDTO.setColor(item.getVariant().getColor());
            itemDTO.setSize(item.getVariant().getSize());
            itemDTO.setQuantity(item.getQuantity());

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