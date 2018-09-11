package com.mtl.hulk.bam;

import com.mtl.hulk.model.AtomicAction;

import java.util.List;

public interface BusinessActivityManager {

    boolean commit();

    boolean rollback();

    List<AtomicAction> enlistAction(AtomicAction action, List<AtomicAction> actions);

    boolean delistAction();

}
