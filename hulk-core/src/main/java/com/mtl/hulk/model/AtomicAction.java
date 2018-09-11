package com.mtl.hulk.model;

public class AtomicAction {

    private String id;
    private AtomicActionCallType callType;
    private ServiceOperation serviceOperation;

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

}
