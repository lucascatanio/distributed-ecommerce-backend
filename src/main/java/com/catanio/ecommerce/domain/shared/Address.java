package com.catanio.ecommerce.domain.shared;

public record Address(
        String street,
        String number,
        String complement,
        String city,
        String state,
        String zipCode,
        String country
) {
    public Address {
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street is required");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City is required");
        if (zipCode == null || zipCode.isBlank()) throw new IllegalArgumentException("ZipCode is required");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country is required");
    }
}
