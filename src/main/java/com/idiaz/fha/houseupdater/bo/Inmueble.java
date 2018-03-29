package com.idiaz.fha.houseupdater.bo;

import java.util.List;

public class Inmueble {

    private String code;

    private String address;

    private Double price;

    private Boolean available;

    private String environments;

    private String propertyRegistry;

    private Double consArea;

    private Double fieldArea;

    private String linkToGo;

    private String city;

    private List<String> images;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getEnvironments() {
        return environments;
    }

    public void setEnvironments(String environments) {
        this.environments = environments;
    }

    public String getPropertyRegistry() {
        return propertyRegistry;
    }

    public void setPropertyRegistry(String propertyRegistry) {
        this.propertyRegistry = propertyRegistry;
    }

    public Double getConsArea() {
        return consArea;
    }

    public void setConsArea(Double consArea) {
        this.consArea = consArea;
    }

    public Double getFieldArea() {
        return fieldArea;
    }

    public void setFieldArea(Double fieldArea) {
        this.fieldArea = fieldArea;
    }

    public String getLinkToGo() {
        return linkToGo;
    }

    public void setLinkToGo(String linkToGo) {
        this.linkToGo = linkToGo;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "Inmueble{" +
                "code='" + code + '\'' +
                ", address='" + address + '\'' +
                ", price=" + price +
                ", available=" + available +
                ", environments='" + environments + '\'' +
                ", propertyRegistry='" + propertyRegistry + '\'' +
                ", consArea=" + consArea +
                ", fieldArea=" + fieldArea +
                ", linkToGo='" + linkToGo + '\'' +
                ", city='" + city + '\'' +
                '}';
    }

}
