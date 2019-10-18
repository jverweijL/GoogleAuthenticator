package com.liferay.demo.googleauthenticator.portlet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.liferay.demo.googleauthenticator.configuration.GoogleAuthenticatorPortletConfiguration;
import com.liferay.demo.googleauthenticator.constants.GoogleAuthenticatorPortletKeys;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.WebKeys;
import org.apache.commons.codec.binary.Base32;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author jverweij
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.auth",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=false",
		"javax.portlet.display-name=GoogleAuthenticator",
		"javax.portlet.init-param.config-template=/configuration.jsp",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + GoogleAuthenticatorPortletKeys.GOOGLEAUTHENTICATOR,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.init-param.add-process-action-success-action=false"
	},
	service = Portlet.class
)
public class GoogleAuthenticatorPortlet extends MVCPortlet {

	//https://github.com/google/google-authenticator/wiki/Key-Uri-Format

	final static int HEIGHT = 250;
	final static int WIDTH = 250;

	final static String ERRORIMAGE = "https://t0.rbxcdn.com/c9bbbc0f504aa381de36c65971591e6d";

	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {

		renderRequest.setAttribute(
				GoogleAuthenticatorPortletConfiguration.class.getName(),
				_googleAuthenticatorPortletConfiguration);

		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		User user = themeDisplay.getUser();

		System.out.println("current user: " + user.getFullName());

		if (user.getExpandoBridge().hasAttribute("GoogleAuthenticatorToken")) {

			if (user.getExpandoBridge().getAttribute("GoogleAuthenticatorToken") == null || user.getExpandoBridge().getAttribute("GoogleAuthenticatorToken").toString().isEmpty()) {
				// should be configurable by instance
				String label = user.getScreenName();
				String issuer = renderRequest.getPreferences().getValue("issuer","lfry");
				if (issuer == null) issuer = "LFRY";
				// key should be stored with user profile
				String secret = this.generateSecretKey();
				System.out.println("secret:" + secret);

				try {
					String barcode = "otpauth://totp/"
							+ label
							+ "?secret=" + secret
							+ "&issuer=" + URLEncoder.encode(issuer,"UTF-8").replace("+", "%20");

					renderRequest.setAttribute("QRCode", "data:image/png;base64," + Base64.getEncoder().encodeToString(getQRCodeImage(barcode, WIDTH, HEIGHT)));
					themeDisplay.getRequest().getSession().setAttribute("userkey",secret);

				} catch (WriterException e) {
					renderRequest.setAttribute("ErrorImage", ERRORIMAGE);
					renderRequest.setAttribute("ErrorMessage", "Error while generating QR Code.");
				}
			}
		} else {
			renderRequest.setAttribute("ErrorImage",ERRORIMAGE);
			renderRequest.setAttribute("ErrorMessage", "MFA not fully configured. User field not available.");
		}

		super.doView(renderRequest, renderResponse);
	}

	private byte[] getQRCodeImage(String text, int width, int height) throws WriterException, IOException {

		ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
		byte[] pngData = pngOutputStream.toByteArray();
		return pngData;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) throws PortalException {
		System.out.println("Hello, put me on a MFA page");
		_googleAuthenticatorPortletConfiguration = ConfigurableUtil.createConfigurable(
				GoogleAuthenticatorPortletConfiguration.class, properties);
	}

	/**
	 * Generate a SecretKey String.
	 *
	 * @return
	 */
	public String generateSecretKey() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		Base32 base32 = new Base32();
		String secretKey = base32.encodeToString(bytes);

		return secretKey;
	}

	private volatile GoogleAuthenticatorPortletConfiguration _googleAuthenticatorPortletConfiguration;
}