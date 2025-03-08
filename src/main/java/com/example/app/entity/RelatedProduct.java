package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Related_Product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatedProduct {

    @EmbeddedId
    private RelatedProductId id;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "Product_ID")
    private Product product;

    @ManyToOne
    @MapsId("relatedProductId")
    @JoinColumn(name = "Related_Product_ID")
    private Product relatedProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "Relation_Type", nullable = false)
    private RelationType relationType;

    public enum RelationType {
        upsell, cross_sell, accessory, similar
    }
}