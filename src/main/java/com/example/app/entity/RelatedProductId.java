package com.example.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatedProductId implements Serializable {

    @Column(name = "Product_ID")
    private Integer productId;

    @Column(name = "Related_Product_ID")
    private Integer relatedProductId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Relation_Type")
    private RelatedProduct.RelationType relationType;
}