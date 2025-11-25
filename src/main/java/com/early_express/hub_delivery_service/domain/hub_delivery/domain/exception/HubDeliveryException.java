package com.early_express.hub_delivery_service.domain.hub_delivery.domain.exception;

import com.early_express.hub_delivery_service.global.presentation.exception.GlobalException;

/**
 * Hub Delivery 도메인 예외
 */
public class HubDeliveryException extends GlobalException {

    public HubDeliveryException(HubDeliveryErrorCode errorCode) {
        super(errorCode);
    }

    public HubDeliveryException(HubDeliveryErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public HubDeliveryException(HubDeliveryErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public HubDeliveryException(HubDeliveryErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
