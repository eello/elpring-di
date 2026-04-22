package eello.app.contoller;

import eello.app.service.Service;
import eello.container.annotation.Component;

@Component
public class AController {

	private final Service service;

	public AController(Service service) {
		this.service = service;
	}

	public void func() {
		System.out.println("AController.func");
		service.func();
	}

	public Service getService() {
		return service;
	}
}
