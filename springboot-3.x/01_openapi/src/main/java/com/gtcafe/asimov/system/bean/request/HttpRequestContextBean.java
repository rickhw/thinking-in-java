package com.gtcafe.asimov.system.bean.request;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class HttpRequestContextBean {
    
    public RequestId getRequestId() {
        return new RequestId();
    }

    
}
