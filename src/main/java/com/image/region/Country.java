/**
 * Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.image.region;

/**
 *
 * @author tritone
 * @version $Id: Country.java, v 0.1 2019年03月24日 00:15 tritone Exp $
 */
public class Country {
    String name;
    Double value;

    public Country() {
    }

    public Country(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}