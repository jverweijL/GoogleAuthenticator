package com.liferay.demo.googleauthenticator;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.exception.PortalException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@Component(
        immediate = true,
        property = {
                "key=login.events.post",
                "service.ranking:Integer=1"
        },
        service = LifecycleAction.class
)
public class LoginEventsPostListener implements LifecycleAction {

    @Override
    public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {
        HttpServletResponse response = lifecycleEvent.getResponse();
        HttpServletRequest request = lifecycleEvent.getRequest();
        HttpSession session = request.getSession();

        session.setAttribute("mfa","redirect");
    }

    @Activate
    @Modified
    protected void activate(Map<String, Object> properties) throws PortalException {

        System.out.println("Post Login MFA Listener started...");

    }
}
