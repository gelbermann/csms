package com.cp.csms.transactions;

import com.cp.csms.common.AuthenticationStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthorizationResponse {

    AuthenticationStatus authenticationStatus;

}
