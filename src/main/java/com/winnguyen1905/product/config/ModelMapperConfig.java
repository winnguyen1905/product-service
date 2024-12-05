package com.winnguyen1905.product.config;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.collection.spi.PersistentCollection;
import org.modelmapper.Condition;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
  @Bean
  ModelMapper modelMapper() {
    List<String> excludes = List.of("createdDate", "updatedDate", "createdBy", "updatedBy");
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
        .getConfiguration()
        .setAmbiguityIgnored(true)
        .setSkipNullEnabled(true)
        .setPropertyCondition(Conditions.isNotNull())
        .setFieldMatchingEnabled(true)
        .setMatchingStrategy(MatchingStrategies.STRICT)
        .setPropertyCondition(new Condition<Object, Object>() {
          public boolean applies(MappingContext<Object, Object> context) {
            return !(context.getSource() instanceof PersistentCollection) &&
                !excludes.contains(context.getMapping().getLastDestinationProperty().getName());
          }
        });
    return modelMapper;
  }
}
