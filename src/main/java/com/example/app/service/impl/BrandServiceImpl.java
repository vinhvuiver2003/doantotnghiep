package com.example.app.service.impl;
import com.example.app.dto.BrandDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.entity.Brand;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.BrandRepository;
import com.example.app.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Autowired
    public BrandServiceImpl(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public PagedResponse<BrandDTO> getAllBrands(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Brand> brands = brandRepository.findAll(pageable);

        List<BrandDTO> content = brands.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                brands.getNumber(),
                brands.getSize(),
                brands.getTotalElements(),
                brands.getTotalPages(),
                brands.isLast()
        );
    }

    @Override
    public BrandDTO getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        return convertToDTO(brand);
    }

    @Override
    @Transactional
    public BrandDTO createBrand(BrandDTO brandDTO) {
        // Check if brand already exists with the same name
        if (brandRepository.existsByName(brandDTO.getName())) {
            throw new IllegalArgumentException("Brand already exists with name: " + brandDTO.getName());
        }

        Brand brand = new Brand();
        brand.setName(brandDTO.getName());
        brand.setDescription(brandDTO.getDescription());
        brand.setLogoUrl(brandDTO.getLogoUrl());

        Brand savedBrand = brandRepository.save(brand);

        return convertToDTO(savedBrand);
    }

    @Override
    @Transactional
    public BrandDTO updateBrand(Integer id, BrandDTO brandDTO) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        // Check if trying to update to a name that already exists for another brand
        if (!brand.getName().equals(brandDTO.getName()) && brandRepository.existsByName(brandDTO.getName())) {
            throw new IllegalArgumentException("Brand already exists with name: " + brandDTO.getName());
        }

        brand.setName(brandDTO.getName());
        brand.setDescription(brandDTO.getDescription());
        brand.setLogoUrl(brandDTO.getLogoUrl());

        Brand updatedBrand = brandRepository.save(brand);

        return convertToDTO(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(Integer id) {
        // Check if brand exists
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found with id: " + id);
        }

        // You might want to check if there are any products with this brand before deletion

        brandRepository.deleteById(id);
    }

    @Override
    public List<BrandDTO> getAllBrandsNoPage() {
        List<Brand> brands = brandRepository.findAll();

        return brands.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Utility method to convert Entity to DTO
    private BrandDTO convertToDTO(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setDescription(brand.getDescription());
        dto.setLogoUrl(brand.getLogoUrl());
        return dto;
    }
}}