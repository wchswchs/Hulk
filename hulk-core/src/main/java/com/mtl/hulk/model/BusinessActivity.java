package com.mtl.hulk.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BusinessActivity {

    private BusinessActivityId id;
    private List<AtomicAction> atomicTryActions = new CopyOnWriteArrayList<>();
    private List<AtomicAction> atomicCommitActions = new CopyOnWriteArrayList<>();
    private List<AtomicAction> atomicRollbackActions = new CopyOnWriteArrayList<>();
    private BusinessActivityId pid = null;
    private long timeout;
    private BusinessActivityStatus status;
    private Date startTime = Calendar.getInstance().getTime();

    public void setId(BusinessActivityId id) {
        this.id = id;
    }

    public void setPid(BusinessActivityId pid) {
        this.pid = pid;
    }

    public void setStatus(BusinessActivityStatus status) {
        this.status = status;
    }

    public void setAtomicTryActions(List<AtomicAction> atomicTryActions) {
        this.atomicTryActions = atomicTryActions;
    }

    public void setAtomicCommitActions(List<AtomicAction> atomicCommitActions) {
        this.atomicCommitActions = atomicCommitActions;
    }

    public void setAtomicRollbackActions(List<AtomicAction> atomicRollbackActions) {
        this.atomicRollbackActions = atomicRollbackActions;
    }

    public BusinessActivityId getId() {
        return id;
    }

    public List<AtomicAction> getAtomicTryActions() {
        return atomicTryActions;
    }

    public List<AtomicAction> getAtomicCommitActions() {
        return atomicCommitActions;
    }

    public List<AtomicAction> getAtomicRollbackActions() {
        return atomicRollbackActions;
    }

    public BusinessActivityId getPid() {
        return pid;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public BusinessActivityStatus getStatus() {
        return status;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

}
