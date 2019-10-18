package com.liferay.demo.googleauthenticator;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;

import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author jverweij
 *
 * For each and every request we need to check whether MFA was finalized for a logged-in user.
 *
 */

@Component(
	immediate = true,
	property = {
			"key=servlet.service.events.pre",
			"service.ranking:Integer=100"
	},
	service = LifecycleAction.class
)
public class ServletPreEventsListener implements LifecycleAction {
	@Override
	public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {

		try {
			System.out.println("servlet.service.events.pre action");
			HttpServletRequest request = lifecycleEvent.getRequest();
			HttpServletResponse response = lifecycleEvent.getResponse();
			HttpSession session = request.getSession();

			String currentUrl = PortalUtil.getCurrentURL(request);
			//System.out.println("currentURL: " + currentUrl);
			User user =  PortalUtil.getUser(request);
			String mfa = (String) session.getAttribute("mfa");

			if (mfa == null)
			{
				mfa = "";
			};

			ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);

			long instanceID = themeDisplay.getCompanyId();
			//System.out.println("instanceID"+instanceID);

			long groupID = Long.parseLong(PortalUtil.getPortalProperties().getProperty("mfa." + instanceID + ".default.siteID"));

			long plid = PortalUtil.getPlidFromPortletId(groupID,
					"com_liferay_demo_googleauthenticator_GoogleAuthenticatorPortlet");
			String mfaUrl = PortalUtil.getLayoutFriendlyURL(LayoutLocalServiceUtil.getLayout(plid), themeDisplay);
			mfaUrl = mfaUrl.substring(mfaUrl.indexOf("/group"));
			//System.out.println("mfaUrl: " + mfaUrl);

			//System.out.println("email match: " + user.getEmailAddress().matches(PortalUtil.getPortalProperties().getProperty("mfa.include.email",".*")));
			//System.out.println("screenname match: " + user.getScreenName().matches(PortalUtil.getPortalProperties().getProperty("mfa.include.screenname",".*")));

			if (user != null &&
				user.getEmailAddress().matches(PortalUtil.getPortalProperties().getProperty("mfa.include.email",".*")) &&
				user.getScreenName().matches(PortalUtil.getPortalProperties().getProperty("mfa.include.screenname",".*")) &&
				currentUrl.indexOf("/c/portal/") < 0 &&
				currentUrl.indexOf(mfaUrl) < 0 &&
				mfa.indexOf("redirect") >= 0) {

				if (user.getExpandoBridge().hasAttribute("GoogleAuthenticatorToken")) {
					response.sendRedirect(mfaUrl);
				}
			}
		} catch (PortalException e) {
			//e.printStackTrace();
			System.out.println("PortalException happened: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Some exception happened: " + e.getMessage());
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) throws PortalException {

		System.out.println("We will check '/group' requests to make sure you correctly entered MFA");

	}

	@Reference
	UserLocalService userlocalService;

}