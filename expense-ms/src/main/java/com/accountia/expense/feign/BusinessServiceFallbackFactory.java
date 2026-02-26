package com.accountia.expense.feign;

import com.accountia.expense.exception.BusinessServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class BusinessServiceFallbackFactory implements FallbackFactory<BusinessServiceClient> {

    @Override
    public BusinessServiceClient create(Throwable cause) {
        return businessId -> {
            throw new BusinessServiceUnavailableException("business-ms is unavailable", cause);
        };
    }
}
