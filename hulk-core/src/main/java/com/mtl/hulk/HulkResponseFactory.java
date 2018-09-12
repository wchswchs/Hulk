package com.mtl.hulk;

import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.model.BusinessActivityStatus;

public class HulkResponseFactory {

    public static HulkResponse getResponse(Integer result) {
        if (result == 1) {
            return new HulkResponse(0, RuntimeContextHolder.getContext().getActivity().getStatus().getDesc());
        }
        if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.COMMITING_FAILED) {
            return new HulkResponse(1, RuntimeContextHolder.getContext().getActivity().getStatus().getDesc());
        }
        return new HulkResponse(2, RuntimeContextHolder.getContext().getActivity().getStatus().getDesc());
    }

}
