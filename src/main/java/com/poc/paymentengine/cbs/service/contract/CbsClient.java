package com.poc.paymentengine.cbs.service.contract;

import com.poc.paymentengine.cbs.dto.request.CbsTransferRequest;

public interface CbsClient {

    void processTransfer(CbsTransferRequest request);
}
