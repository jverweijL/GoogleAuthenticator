# Setup

in portal-ext.properties set default mfa site for the instance

mfa.[INSTANCE_ID].default.siteID=20142  

`mfa.20115.default.siteID=20142`

we might change this later based on default site set with user profile.

To control which users will need to use MFA you can set the following properties
```
mfa.include.email=.*@liferay.com
mfa.include.screenname=klaa.*
```

Within this default site you must put the mfa portlet on a page.

Once you've done this you can configure custom user fields:

**GoogleAuthenticatorToken**  
- Type: Text
- Secret: True
- Searchable: false

**GoogleAuthenticatorTokenTries**
- Type: Integer (32-bit)
- Searchable: false

Make sure to give User '*View*' and '*Update*' permissions on both fields.