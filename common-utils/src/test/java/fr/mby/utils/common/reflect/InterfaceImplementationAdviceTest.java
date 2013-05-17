/**
 * 
 */
package fr.mby.utils.common.reflect;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @author Maxime Bossard - 2013.
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class InterfaceImplementationAdviceTest {

	private static final String ADVISED_RESULT = "advised result !";
	
	@Test
	public void test1() {
		String target = "test42 !";
		ProxyFactory proxyFactory = new ProxyFactory(target);
		proxyFactory.addInterface(IAdvisedInterface.class);
		
		AdvisedImplementation advisedImpl = new AdvisedImplementation();
		proxyFactory.addAdvice(new InterfaceImplementationAdvice(IAdvisedInterface.class, advisedImpl));
		
		Object proxyTarget = proxyFactory.getProxy();
		
		IAdvisedInterface advisedTarget = (IAdvisedInterface) proxyTarget;
		
		Assert.assertEquals("Bad advised implementation", "test42 !", advisedTarget.toString());
		Assert.assertEquals("Bad advised implementation", InterfaceImplementationAdviceTest.ADVISED_RESULT, advisedTarget.advisedMethod());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test2() {
		AdvisedImplementation advisedImpl = new AdvisedImplementation();
		new InterfaceImplementationAdvice(AdvisedImplementation.class, advisedImpl);
	}
	
	private interface IAdvisedInterface {
		String advisedMethod();
	}
	
	private class AdvisedImplementation implements IAdvisedInterface {
		@Override
		public String advisedMethod() {
			return InterfaceImplementationAdviceTest.ADVISED_RESULT;
		}
	}
}
