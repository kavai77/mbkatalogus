package com.himadri.engine;

import com.himadri.model.ErrorCollector;
import com.himadri.model.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
public class UserSession {
    private static final String USER_REQUEST = "USER_REQUEST";
    private static final String ERROR_COLLECTOR = "ERROR_COLLECTOR";

    @Autowired
    private HttpServletRequest httpServletRequest;

    public UserRequest getUserRequest() {
        return (UserRequest) getSession().getAttribute(USER_REQUEST);
    }

    public void setUserRequest(UserRequest userRequest) {
        getSession().setAttribute(USER_REQUEST, userRequest);
    }

    public ErrorCollector getErrorCollector() {
        ErrorCollector errorCollector = (ErrorCollector) getSession().getAttribute(ERROR_COLLECTOR);
        if (errorCollector == null) {
            errorCollector = new ErrorCollector();
            getSession().setAttribute(ERROR_COLLECTOR, errorCollector);
        }
        return errorCollector;
    }

    private HttpSession getSession() {
        return httpServletRequest.getSession();
    }
}
