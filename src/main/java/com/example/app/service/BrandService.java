package com.example.app.service;

import com.example.app.dto.BrandDTO;
import com.example.app.dto.PagedResponse;
import java.util.List;
import java.util.List;

public interface BrandService {
    PagedResponse<BrandDTO> getAllBrands(int page, int size, String sortBy, String sortDir);

    BrandDTO getBrandById(Integer id);

    BrandDTO createBrand(BrandDTO brandDTO);

    BrandDTO updateBrand(Integer id, BrandDTO brandDTO);

    void deleteBrand(Integer id);

    List<BrandDTO> getAllBrandsNoPage();
}