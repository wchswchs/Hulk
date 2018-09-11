package com.mtl.hulk;

import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.model.AtomicAction;
import org.springframework.context.ApplicationContext;

public abstract class HulkListener extends AbstractHulk {

    protected BusinessActivityManagerImpl bam;
    protected volatile AtomicAction action;

    public HulkListener(BusinessActivityManagerImpl bam, AtomicAction action, HulkDataSource ds, ApplicationContext applicationContext) {
        super(ds, applicationContext);
        this.bam = bam;
        this.action = action;
    }

    public HulkListener(BusinessActivityManagerImpl bam, AtomicAction action, HulkDataSource ds) {
        super(ds);
        this.bam = bam;
        this.action = action;
    }

    public HulkListener(AtomicAction action, HulkDataSource ds, ApplicationContext context) {
        super(ds, context);
        this.action = action;
    }

    public HulkListener(HulkDataSource ds) {
        super(ds);
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
