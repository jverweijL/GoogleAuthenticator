package com.liferay.demo.googleauthenticator.portlet.action;

import com.liferay.demo.googleauthenticator.constants.GoogleAuthenticatorPortletKeys;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.component.annotations.Component;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

@Component(
        immediate = true,
        property = {
                "javax.portlet.name=" + GoogleAuthenticatorPortletKeys.GOOGLEAUTHENTICATOR,
                "mvc.command.name=storekey"
        },
        service = MVCActionCommand.class
)

public class StoreKeyMVCActionCommand extends BaseMVCActionCommand {

        @Override
        protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
                ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
                User user = themeDisplay.getUser();
                String secret = (String)themeDisplay.getRequest().getSession().getAttribute("userkey");
                user.getExpandoBridge().setAttribute("GoogleAuthenticatorToken",secret,true);
                themeDisplay.getRequest().getSession().removeAttribute("userkey");
        }
}
