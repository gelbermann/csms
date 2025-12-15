package com.cp.csms.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthenticationResponse {

    String requestId;
    AuthenticationStatus status;

}
