package com.example.app.repository;


import com.example.app.entity.RelatedProduct;
import com.example.app.entity.RelatedProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatedProductRepository extends JpaRepository<RelatedProduct, RelatedProductId> {
    @Query("SELECT rp FROM RelatedProduct rp WHERE rp.product.id = :productId")
    List<RelatedProduct> findByProductId(@Param("productId") Integer productId);

    @Query("SELECT rp FROM RelatedProduct rp WHERE rp.product.id = :productId AND rp.relationType = :relationType")
    List<RelatedProduct> findByProductIdAndRelationType(@Param("productId") Integer productId,
                                                        @Param("relationType") RelatedProduct.RelationType relationType);

    void deleteByProductId(Integer productId);

    void deleteByRelatedProductId(Integer relatedProductId);
}