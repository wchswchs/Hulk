package com.mtl.hulk;

import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.model.AtomicAction;
import org.springframework.context.ApplicationContext;

public abstract class HulkListener extends AbstractHulk {

    protected volatile AtomicAction action;

    public HulkListener(AtomicAction action, ApplicationContext context) {
        super(context);
        this.action = action;
    }

    public HulkListener(HulkProperties properties, ApplicationContext apc) {
        super(properties, apc);
    }

    public void setAction(AtomicAction action) {
        this.action = action;
    }

    public abstract boolean process();

}
