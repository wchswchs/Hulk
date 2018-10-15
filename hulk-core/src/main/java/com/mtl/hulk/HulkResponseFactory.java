package com.mtl.hulk;

import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.model.BusinessActivityStatus;

public class HulkResponseFactory {

    public static HulkResponse getResponse(boolean result) {
        if (result) {
            return new HulkResponse(0, RuntimeContextHolder.getContext().getActivity().getStatus().getDesc(), null);
        }
        if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.COMMITING_FAILED) {
            return new HulkResponse(1, RuntimeContextHolder.getContext().getActivity().getStatus().getDesc(),
                                    RuntimeContextHolder.getContext().getException());
        }
        return new HulkResponse(2, RuntimeContextHolder.getContext().getActivity().getStatus().getDesc(),
                                RuntimeContextHolder.getContext().getException());
    }

}
