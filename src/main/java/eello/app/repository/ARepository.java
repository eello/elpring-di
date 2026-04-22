package eello.app.repository;

import eello.container.annotation.Component;

@Component
public class ARepository implements Repository {

	@Override
	public void func() {
		System.out.println("ARepository.func");
	}
}
