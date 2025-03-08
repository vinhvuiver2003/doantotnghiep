package com.example.app.service.impl;
import com.example.app.dto.ProductVariantDTO;
import com.example.app.entity.Product;
import com.example.app.entity.ProductVariant;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.ProductVariantRepository;
import com.example.app.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProductVariantServiceImpl(
            ProductVariantRepository variantRepository,
            ProductRepository productRepository) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductVariantDTO> getVariantsByProduct(Integer productId) {
        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        return variants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVariantDTO getVariantById(Integer id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + id));

        return convertToDTO(variant);
    }

    @Override
    public List<ProductVariantDTO> getAvailableVariantsByProduct(Integer productId) {
        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<ProductVariant> variants = variantRepository.findAvailableVariants(productId);
        return variants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductVariantDTO createVariant(ProductVariantDTO variantDTO) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(variantDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + variantDTO.getProductId()));

        // Kiểm tra nếu biến thể với color và size đã tồn tại
        variantRepository.findByProductIdAndColorAndSize(
                        variantDTO.getProductId(), variantDTO.getColor(), variantDTO.getSize())
                .ifPresent(v -> {
                    throw new IllegalArgumentException("Variant with the same color and size already exists");
                });

        // Tạo biến thể mới
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(variantDTO.getColor());
        variant.setSize(variantDTO.getSize());
        variant.setStockQuantity(variantDTO.getStockQuantity());
        variant.setPriceAdjustment(variantDTO.getPriceAdjustment());
        variant.setImage(variantDTO.getImage());

        // Đặt trạng thái dựa trên tồn kho
        if (variantDTO.getStockQuantity() <= 0) {
            variant.setStatus(ProductVariant.VariantStatus.out_of_stock);
        } else if (variantDTO.getStatus() != null) {
            variant.setStatus(ProductVariant.VariantStatus.valueOf(variantDTO.getStatus()));
        } else {
            variant.setStatus(ProductVariant.VariantStatus.active);
        }

        ProductVariant savedVariant = variantRepository.save(variant);
        return convertToDTO(savedVariant);
    }

    @Override
    @Transactional
    public ProductVariantDTO updateVariant(Integer id, ProductVariantDTO variantDTO) {
        // Kiểm tra biến thể tồn tại
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + id));

        // Kiểm tra nếu đang thay đổi color/size và biến thể mới đã tồn tại
        if (!variant.getColor().equals(variantDTO.getColor()) || !variant.getSize().equals(variantDTO.getSize())) {
            variantRepository.findByProductIdAndColorAndSize(
                            variant.getProduct().getId(), variantDTO.getColor(), variantDTO.getSize())
                    .ifPresent(v -> {
                        if (!v.getId().equals(id)) {
                            throw new IllegalArgumentException("Variant with the same color and size already exists");
                        }
                    });
        }

        // Cập nhật thông tin
        variant.setColor(variantDTO.getColor());
        variant.setSize(variantDTO.getSize());
        variant.setStockQuantity(variantDTO.getStockQuantity());
        variant.setPriceAdjustment(variantDTO.getPriceAdjustment());

        if (variantDTO.getImage() != null) {
            variant.setImage(variantDTO.getImage());
        }

        // Cập nhật trạng thái dựa trên tồn kho
        if (variantDTO.getStockQuantity() <= 0) {
            variant.setStatus(ProductVariant.VariantStatus.out_of_stock);
        } else if (variantDTO.getStatus() != null) {
            variant.setStatus(ProductVariant.VariantStatus.valueOf(variantDTO.getStatus()));
        }

        ProductVariant updatedVariant = variantRepository.save(variant);
        return convertToDTO(updatedVariant);
    }

    @Override
    @Transactional
    public ProductVariantDTO updateVariantStock(Integer id, Integer quantity) {
        // Kiểm tra biến thể tồn tại
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + id));

        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        // Cập nhật tồn kho
        variant.setStockQuantity(quantity);

        // Cập nhật trạng thái dựa trên tồn kho
        if (quantity <= 0) {
            variant.setStatus(ProductVariant.VariantStatus.out_of_stock);
        } else if (variant.getStatus() == ProductVariant.VariantStatus.out_of_stock) {
            variant.setStatus(ProductVariant.VariantStatus.active);
        }

        ProductVariant updatedVariant = variantRepository.save(variant);
        return convertToDTO(updatedVariant);
    }

    @Override
    @Transactional
    public ProductVariantDTO updateVariantStatus(Integer id, String status) {
        // Kiểm tra biến thể tồn tại
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + id));

        try {
            ProductVariant.VariantStatus newStatus = ProductVariant.VariantStatus.valueOf(status);

            // Kiểm tra nếu đang đặt trạng thái active nhưng hết hàng
            if (newStatus == ProductVariant.VariantStatus.active && variant.getStockQuantity() <= 0) {
                throw new IllegalArgumentException("Cannot set status to active when stock is empty");
            }

            variant.setStatus(newStatus);
            ProductVariant updatedVariant = variantRepository.save(variant);
            return convertToDTO(updatedVariant);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional
    public void deleteVariant(Integer id) {
        // Kiểm tra biến thể tồn tại
        if (!variantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product variant not found with id: " + id);
        }

        // Xóa biến thể
        variantRepository.deleteById(id);
    }

    @Override
    public List<ProductVariantDTO> getLowStockVariants(Integer threshold) {
        List<ProductVariant> variants = variantRepository.findLowStockVariants(threshold);
        return variants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Utility method to convert Entity to DTO
    private ProductVariantDTO convertToDTO(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(variant.getId());
        dto.setProductId(variant.getProduct().getId());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setStockQuantity(variant.getStockQuantity());
        dto.setPriceAdjustment(variant.getPriceAdjustment());

        // Tính giá cuối cùng = giá cơ bản + phụ thu
        BigDecimal finalPrice = variant.getProduct().getBasePrice().add(variant.getPriceAdjustment());
        dto.setFinalPrice(finalPrice);

        dto.setImage(variant.getImage());
        dto.setStatus(variant.getStatus().name());

        // Những thông tin liên quan khác như danh sách hình ảnh biến thể có thể được thêm ở đây

        return dto;
    }
}