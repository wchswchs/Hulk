package com.mtl.hulk.bam;

import com.mtl.hulk.model.AtomicAction;
import org.aopalliance.intercept.MethodInvocation;

public interface UserBusinessActivity {

    boolean start(AtomicAction action, MethodInvocation methodInvocation);
}
