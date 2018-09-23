package com.mtl.hulk.model;

public class AtomicAction implements Comparable<AtomicAction> {

    private String id;
    private AtomicActionCallType callType;
    private ServiceOperation serviceOperation;
    private Integer order = -1;

    public void setId(String id) {
        this.id = id;
    }

    public void setCallType(AtomicActionCallType callType) {
        this.callType = callType;
    }

    public void setServiceOperation(ServiceOperation serviceOperation) {
        this.serviceOperation = serviceOperation;
    }

    public AtomicActionCallType getCallType() {
        return callType;
    }

    public ServiceOperation getServiceOperation() {
        return serviceOperation;
    }

    public String getId() {
        return id;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getOrder() {
        return order;
    }

    @Override
    public int compareTo(AtomicAction o) {
        return order.compareTo(o.getOrder());
    }

}
