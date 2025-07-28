/*package com.example.catalogservice.config;

import com.example.catalogservice.entity.Catalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO) //PagedModel ilə @EnableSpringDataWebSupport Həlli

public class WebConfig implements WebMvcConfigurer {

    @Bean(name = "customPagedResourcesAssembler")
    public PagedResourcesAssembler<Catalog> pagedResourcesAssembler() {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("localhost")
                .build();
        return new PagedResourcesAssembler<>(new HateoasPageableHandlerMethodArgumentResolver(), uriComponents);
    }

}*/
