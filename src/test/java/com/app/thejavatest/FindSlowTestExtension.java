package com.app.thejavatest;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class FindSlowTestExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

	private static final long THRESHOLD = 1000L;

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		String testMethodName = context.getRequiredTestMethod().getName();
		ExtensionContext.Store store = getStore(context);

		SlowTest annotation = context.getRequiredTestMethod().getAnnotation(SlowTest.class);
		Long start_time = store.remove("START_TIME", long.class);
		long duration = System.currentTimeMillis() - start_time;
		if (duration > THRESHOLD && annotation == null) {
			System.out.printf("Please consider mark method [%s] with @SlowTest.\n", testMethodName);
		}
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		ExtensionContext.Store store = getStore(context);
		store.put("START_TIME", System.currentTimeMillis());
	}

	private ExtensionContext.Store getStore(
		ExtensionContext context
	) {
		String testClassName = context.getRequiredTestClass().getName();
		String testMethodName = context.getRequiredTestMethod().getName();
		return context.getStore(
			ExtensionContext.Namespace.create(testClassName, testMethodName));
	}
}
