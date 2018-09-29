package com.mtl.hulk;

import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.model.AtomicAction;
import org.springframework.context.ApplicationContext;

public abstract class HulkListener extends AbstractHulk {

    protected BusinessActivityManagerImpl bam;
    protected volatile AtomicAction action;

    public HulkListener(AtomicAction action, ApplicationContext context) {
        super(context);
        this.action = action;
    }

    public HulkListener(BusinessActivityManagerImpl bam, ApplicationContext apc) {
        super(apc);
        this.bam = bam;
    }

    public void setBam(BusinessActivityManagerImpl bam) {
        this.bam = bam;
    }

    public BusinessActivityManagerImpl getBam() {
        return bam;
    }

    public void setAction(AtomicAction action) {
        this.action = action;
    }

    public abstract boolean process();

}
