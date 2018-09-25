package com.mtl.hulk.aop;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import org.springframework.beans.factory.BeanFactory;

public abstract class HulkAspectSupport extends AbstractHulk {

    private BeanFactory beanFactory;
    protected BusinessActivityManagerImpl bam;

    public HulkAspectSupport(BusinessActivityManagerImpl bam) {
        super();
        this.bam = bam;
    }

}
