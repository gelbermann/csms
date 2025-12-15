package com.cp.csms.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthenticationMessage {

    String requestId;
    String token;

}
