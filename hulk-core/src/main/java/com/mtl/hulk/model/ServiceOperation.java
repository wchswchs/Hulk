package com.mtl.hulk.model;

public class ServiceOperation {

    private String name;
    private String beanClass;
    private ServiceOperationType type;

    public ServiceOperationType getType() {
        return type;
    }

    public void setType(ServiceOperationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBeanClass(String beanClass) {
        this.beanClass = beanClass;
    }

    public String getBeanClass() {
        return beanClass;
    }

}
