/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.brand;

import com.zipato.model.NameObjectRepository;

/**
 * Created by murielK on 8/13/2014.
 */
public class BrandRepository extends NameObjectRepository<Brand> {

    public void fetchAll() {

        Brand[] list = factory.getRestTemplate().getForObject("v2/brands", Brand[].class);
        clear();
        for (Brand b : list) {
            fetchBrand(b.getName());
        }

    }

    public void fetchBrand(String brandName) {
        Brand brand = factory.getRestTemplate().getForObject("v2/brands/{name}", Brand.class, brandName);
        add(brand);
    }

}
