package com.cp.csms.authentication.validation;

import com.cp.csms.common.AuthenticationMessage;

public interface AuthenticationValidator {

    boolean validate(AuthenticationMessage message);

}
