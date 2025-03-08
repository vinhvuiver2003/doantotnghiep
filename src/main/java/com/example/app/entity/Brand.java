package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Brand")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Brand_ID")
    private Integer id;

    @Column(name = "Brand_name", unique = true, nullable = false)
    private String name;

    @Column(name = "Brand_desc")
    private String description;

    @Column(name = "Logo_url")
    private String logoUrl;
}