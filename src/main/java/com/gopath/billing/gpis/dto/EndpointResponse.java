package com.gopath.billing.gpis.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EndpointResponse<T> {
    private int code;
    private String message;
    private T result;
}
