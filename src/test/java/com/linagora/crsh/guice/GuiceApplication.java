package com.linagora.crsh.guice;

import java.util.Random;


import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.linagora.crsh.guice.CrashGuiceConfiguration;
import com.linagora.crsh.guice.CrashGuiceSupport;

public class GuiceApplication extends ServletModule {
	
	@Override
	protected void configureServlets() {
		super.configureServlets();
		int telnetPortValue = new Random().nextInt(10000) + 1025;
		bind(Integer.class).annotatedWith(Names.named("telnet.port")).toInstance(telnetPortValue);
		install(new CrashGuiceSupport());
		serve("/").with(SampleServlet.class);
	}
	
	@Provides
	public CrashGuiceConfiguration configuration(@Named("telnet.port") int port) {
		return CrashGuiceConfiguration.builder().property("telnet.port", port).build();
	}
	
}
