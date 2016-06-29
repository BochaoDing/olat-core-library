package org.olat.repository.ui.list;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Configuration
public class RepositoryEntryBeanFactory {

	@Autowired
	private RepositoryModule repositoryModule;

	@Autowired
	private MapperService mapperService;

	@Bean(name={"row_1"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	protected VelocityContainer createRow1VelocityContainer(BasicController caller) {
		VelocityContainer result = new VelocityContainer(null,
				"vc_" + "row_1",
				Util.getPackageVelocityRoot(RepositoryEntryBeanFactory.class) + "/row_1.html",
				Util.createPackageTranslator(caller.getClass(), caller.getLocale()),
				caller
		);
		result.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		return result;
	}

	@Bean(name={"RepositoryEntryRowFactory"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	protected RepositoryEntryRowFactory createRepositoryEntryRowFactory(UserRequest userRequest) {
		return new RepositoryEntryRowFactory(repositoryModule, mapperService, userRequest);
	}
}
