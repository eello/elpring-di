package eello.app;

import eello.app.contoller.AController;
import eello.app.repository.ARepository;
import eello.app.service.AService;
import eello.app.service.BService;
import eello.container.Application;
import eello.container.core.BeanFactory;

import java.lang.reflect.InvocationTargetException;

public class SimpleDIContainer {

	public static void main(String[] args) throws
		ClassNotFoundException,
		InvocationTargetException,
		InstantiationException,
		IllegalAccessException {

		BeanFactory context = Application.run(SimpleDIContainer.class);

		AController aController = context.getBean("aController", AController.class);
		AService aService = context.getBean("aService", AService.class);
		BService bService = context.getBean("bService", BService.class);
		ARepository aRepository = context.getBean("aRepository", ARepository.class);

		System.out.println("==================================");

		aController.func();

		System.out.println("==================================");

		System.out.println("aController.getService() == aService ? " + (aController.getService() == aService));
		System.out.println("aController.getService() != bService ? " + (aController.getService() != bService));
		System.out.println("aService.getRepository() == aRepository ? " + (aService.getRepository() == aRepository));
	}
}
