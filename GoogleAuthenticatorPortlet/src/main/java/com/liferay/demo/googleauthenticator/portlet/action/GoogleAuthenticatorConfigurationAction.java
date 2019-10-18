package com.liferay.demo.googleauthenticator.portlet.action;
    import com.liferay.demo.googleauthenticator.configuration.GoogleAuthenticatorPortletConfiguration;
    import com.liferay.demo.googleauthenticator.constants.GoogleAuthenticatorPortletKeys;
    import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
    import com.liferay.portal.kernel.log.Log;
    import com.liferay.portal.kernel.log.LogFactoryUtil;
    import com.liferay.portal.kernel.portlet.ConfigurationAction;
    import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
    import com.liferay.portal.kernel.util.ParamUtil;

    import java.util.Map;

    import javax.portlet.ActionRequest;
    import javax.portlet.ActionResponse;
    import javax.portlet.PortletConfig;

    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;

    import org.osgi.service.component.annotations.Activate;
    import org.osgi.service.component.annotations.Component;
    import org.osgi.service.component.annotations.ConfigurationPolicy;
    import org.osgi.service.component.annotations.Modified;

/**
 * @author Liferay
 */
@Component(
        configurationPid = "com.liferay.blade.samples.configurationaction.MessageDisplayConfiguration",
        configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
        property = "javax.portlet.name=" + GoogleAuthenticatorPortletKeys.GOOGLEAUTHENTICATOR,
        service = ConfigurationAction.class
)
public class GoogleAuthenticatorConfigurationAction
        extends DefaultConfigurationAction {

    @Override
    public void include(
            PortletConfig portletConfig, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws Exception {

        if (_log.isInfoEnabled()) {
            _log.info("GoogleAuthenticatorConfiguration include");
        }

        httpServletRequest.setAttribute(
                GoogleAuthenticatorPortletConfiguration.class.getName(),
                _googleAuthenticatorConfiguration);

        super.include(portletConfig, httpServletRequest, httpServletResponse);
    }

    @Override
    public void processAction(
            PortletConfig portletConfig, ActionRequest actionRequest,
            ActionResponse actionResponse)
            throws Exception {

        if (_log.isInfoEnabled()) {
            _log.info("GoogleAuthenticatorPortlet configuration action");
        }

        String issuer = ParamUtil.getString(actionRequest, "issuer");
        String maxtries = ParamUtil.getString(actionRequest, "maxtries");

        if (_log.isInfoEnabled()) {
            _log.info("Message Display Configuration: Issuer: " + issuer);
        }

        setPreference(actionRequest, "issuer", issuer);
        setPreference(actionRequest, "maxtries", maxtries);

        super.processAction(portletConfig, actionRequest, actionResponse);
    }

    @Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
        _googleAuthenticatorConfiguration = ConfigurableUtil.createConfigurable(
                GoogleAuthenticatorPortletConfiguration.class, properties);
    }

    private static final Log _log = LogFactoryUtil.getLog(GoogleAuthenticatorConfigurationAction.class);

    private volatile GoogleAuthenticatorPortletConfiguration _googleAuthenticatorConfiguration;

}