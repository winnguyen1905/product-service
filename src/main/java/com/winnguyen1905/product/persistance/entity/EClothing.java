package com.winnguyen1905.product.persistance.entity;

 
import com.winnguyen1905.product.core.common.ProductTypeConstant;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "clothings")
@DiscriminatorValue(ProductTypeConstant.CLOTHING)
@PrimaryKeyJoinColumn(name = "clothing_id")
public class EClothing extends EProduct {
    
}
