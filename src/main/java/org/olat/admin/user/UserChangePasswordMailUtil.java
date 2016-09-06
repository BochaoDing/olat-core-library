package org.olat.admin.user;

import ch.uzh.campus.mapper.UserMappingDeletion;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;

public class UserChangePasswordMailUtil {

    private String dummyKey;

    @Autowired
    private MailManager mailManager;
    @Autowired
    private RegistrationManager registrationManager;

    private static final OLog LOG = Tracing.createLoggerFor(UserChangePasswordMailUtil.class);

    public UserChangePasswordMailUtil() {
    }

    private Locale getUserLocale(Identity user) {
        Preferences prefs = user.getUser().getPreferences();
        return I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());

    }

    public String generateMailText(Identity user) throws UserHasNoEmailException {
        Locale locale = getUserLocale(user);
        String emailAdress = user.getUser().getProperty(UserConstants.EMAIL, locale);
        if (emailAdress == null) {
            throw new UserHasNoEmailException("No email specified for " + user.getName());
        }

        String serverpath = Settings.getServerContextPathURI();
        Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale) ;

        return userTrans.translate("pwchange.intro", new String[] { user.getName() })
                + userTrans.translate("pwchange.body", new String[] {
                serverpath, getDummyKey(emailAdress), I18nManager.getInstance().getLocaleKey(locale)
        });
    }

    public String getDummyKey(String emailAdress) {
        return Encoder.md5hash(emailAdress);
    }

    public MailerResult sendTokenByMail(UserRequest ureq, Identity user, String text) throws UserChangePasswordException, UserHasNoEmailException {
        // check if user has an OLAT provider token, otherwhise a pwd change makes no sense
        Authentication auth = BaseSecurityManager.getInstance().findAuthentication(user, BaseSecurityModule.getDefaultAuthProviderIdentifier());
        if (auth == null) {
            LOG.error(user.getName() + " has no OLAT provider token");
            throw new UserChangePasswordException(user.getName() + " has no OLAT provider token");
        }

        Locale locale = getUserLocale(user);
        String emailAdress = user.getUser().getProperty(UserConstants.EMAIL, locale);
        if (emailAdress == null) {
            LOG.error("No email specified for " + user.getName());
            throw new UserHasNoEmailException("No email specified for " + user.getName());
        }

        // Validate if template corresponds to our expectations (should containt dummy key)
        if (!text.contains(getDummyKey(emailAdress))) {
            LOG.error("Dummy key not found in prepared email");
            throw new UserChangePasswordException("Dummy key not found in prepared email");
        }

        TemporaryKey tk = registrationManager.loadTemporaryKeyByEmail(emailAdress);
        if (tk == null) {
            String ip = ureq.getHttpReq().getRemoteAddr();
            tk = registrationManager.createTemporaryKeyByEmail(emailAdress, ip, RegistrationManager.PW_CHANGE);
        }
        String body = text.replace(dummyKey, tk.getRegistrationKey());
        Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale) ;

        MailBundle bundle = new MailBundle();
        bundle.setToId(user);
        bundle.setContent(userTrans.translate("pwchange.subject"), body);
        return mailManager.sendExternMessage(bundle, null, false);
    }

}

