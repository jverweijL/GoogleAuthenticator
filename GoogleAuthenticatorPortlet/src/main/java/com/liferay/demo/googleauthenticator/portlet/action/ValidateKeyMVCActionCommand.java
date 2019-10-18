package com.liferay.demo.googleauthenticator.portlet.action;

import com.liferay.demo.googleauthenticator.configuration.GoogleAuthenticatorPortletConfiguration;
import com.liferay.demo.googleauthenticator.constants.GoogleAuthenticatorPortletKeys;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import org.apache.commons.codec.binary.Base32;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import java.security.GeneralSecurityException;
import java.util.Map;

@Component(
        immediate = true,
        property = {
            "javax.portlet.name=" + GoogleAuthenticatorPortletKeys.GOOGLEAUTHENTICATOR,
            "mvc.command.name=validatekey"
        },
        service = MVCActionCommand.class
)

public class ValidateKeyMVCActionCommand extends BaseMVCActionCommand {

        @Override
        protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

                ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
                User user = themeDisplay.getUser();

                PortletPreferences preferences = actionRequest.getPreferences();

                Integer maxtries = Integer.valueOf(preferences.getValue("maxtries",_googleAuthenticatorPortletConfiguration.maxtries()));
                Integer attempts = Integer.valueOf(user.getExpandoBridge().getAttribute("GoogleAuthenticatorTokenTries",false).toString());

                // if valid key
                String secret = user.getExpandoBridge().getAttribute("GoogleAuthenticatorToken",true).toString();
                int code = generateNumber(secret,System.currentTimeMillis(),30);
                int verificationcode = ParamUtil.getInteger(actionRequest, "verificationcode");

                if (Integer.compare(code,verificationcode) == 0 && attempts < maxtries) {
                    themeDisplay.getRequest().getSession().setAttribute("mfa", "validated");
                    user.getExpandoBridge().setAttribute("GoogleAuthenticatorTokenTries",0,false);
                    //TODO make this dynamic/configurable
                    actionResponse.sendRedirect(preferences.getValue("starturl",_googleAuthenticatorPortletConfiguration.starturl()));
                } else {
                        attempts += 1;
                        user.getExpandoBridge().setAttribute("GoogleAuthenticatorTokenTries",attempts,false);
                }
        }

        public int generateNumber(String base32Secret, long timeMillis, int timeStepSeconds)
                throws GeneralSecurityException {

                Base32 base32 = new Base32();
                byte[] key = base32.decode(base32Secret);

                byte[] data = new byte[8];
                long value = timeMillis / 1000 / timeStepSeconds;
                for (int i = 7; value > 0; i--) {
                        data[i] = (byte) (value & 0xFF);
                        value >>= 8;
                }

                // encrypt the data with the key and return the SHA1 of it in hex
                SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
                // if this is expensive, could put in a thread-local
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(signKey);
                byte[] hash = mac.doFinal(data);

                // take the 4 least significant bits from the encrypted string as an offset
                int offset = hash[hash.length - 1] & 0xF;

                // We're using a long because Java hasn't got unsigned int.
                long truncatedHash = 0;
                for (int i = offset; i < offset + 4; ++i) {
                        truncatedHash <<= 8;
                        // get the 4 bytes at the offset
                        truncatedHash |= (hash[i] & 0xFF);
                }
                // cut off the top bit
                truncatedHash &= 0x7FFFFFFF;

                // the token is then the last 6 digits in the number
                truncatedHash %= 1000000;
                // this is only 6 digits so we can safely case it
                return (int) truncatedHash;
        }

        @Activate
        @Modified
        protected void activate(Map<String, Object> properties) throws PortalException {
                System.out.println("Hello action");
                _googleAuthenticatorPortletConfiguration = ConfigurableUtil.createConfigurable(
                        GoogleAuthenticatorPortletConfiguration.class, properties);
        }

        private volatile GoogleAuthenticatorPortletConfiguration _googleAuthenticatorPortletConfiguration;

        private static final Log _log = LogFactoryUtil.getLog(ValidateKeyMVCActionCommand.class);
}
