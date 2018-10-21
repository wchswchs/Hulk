package com.mtl.hulk.logger;

import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.snapshot.HulkFileCallback;
import com.mtl.hulk.serializer.KryoSerializer;

public class BusinessActivityLogCallback extends HulkFileCallback<HulkContext> {

    public BusinessActivityLogCallback(BusinessActivityLogger bal) {
        super(bal);
    }

    @Override
    public void process(byte[] data) throws Exception {
        HulkContext ctx =  KryoSerializer.deserialize(data, HulkContext.class);
        ((BusinessActivityLogger) obj).write(ctx.getRc(), ctx.getBac());
    }

}
