package com.liferay.demo.googleauthenticator.configuration;

import aQute.bnd.annotation.metatype.Meta;

/**
 * @author Liferay
 */
@Meta.OCD(
    id = "com.liferay.demo.googleauthenticator.configuration.GoogleAuthenticatorPortletConfiguration",
    name = "GoogleAuthenticatorPortletConfiguration-name"
)
public interface GoogleAuthenticatorPortletConfiguration {

    @Meta.AD(
            required = false,
            deflt = "liferay portal",
            name = "issuer",
            description = "enter issuer name that will be shown in authenticator app"
    )
    public String issuer();


    @Meta.AD(
            required = false,
            deflt = "3",
            name = "maxtries",
            description = "Maximum number of tries before lock-down"
    )
    public String maxtries();

    @Meta.AD(
            required = false,
            deflt = "/group/guest/home",
            name = "starturl",
            description = "where to redirect on success"
    )
    public String starturl();

}