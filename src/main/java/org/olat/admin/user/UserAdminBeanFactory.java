package org.olat.admin.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;

/**
 * @author Martin Schraner
 */
@Configuration
public class UserAdminBeanFactory {

	@Bean(name={"userAdminControllerAdditionalTabs"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public UserAdminControllerAdditionalTabs userAdminControllerAdditionalTabs(UserRequest userRequest, WindowControl windowControl, Identity identity) {
		// Return empty list, i.e. no additional tabs
		return new UserAdminControllerAdditionalTabs(new ArrayList<>());
	}
}
