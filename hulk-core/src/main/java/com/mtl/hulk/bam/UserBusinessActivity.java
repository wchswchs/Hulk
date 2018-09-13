package com.mtl.hulk.bam;

import org.aopalliance.intercept.MethodInvocation;

public interface UserBusinessActivity {

    boolean start(MethodInvocation methodInvocation);
}
