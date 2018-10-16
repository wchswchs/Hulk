package com.mtl.hulk;

import com.mtl.hulk.model.BusinessIdForSequence;
import com.mtl.hulk.sequence.IncrTimeSequence;
import org.apache.commons.lang3.StringUtils;

public class BusinessActivityIdSequenceFactory {

    public static HulkSequence getSequence(String sequence){
        if(StringUtils.equals("timestamp", sequence)){
            return IncrTimeSequence.getInstance(BusinessIdForSequence.TRANSACTION_ID.value());
        }
        return null;
    }

}
