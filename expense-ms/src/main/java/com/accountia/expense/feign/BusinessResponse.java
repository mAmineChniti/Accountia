package com.accountia.expense.feign;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessResponse {
    private Long id;
    private String name;
    private String status;
    private String currency;
}
