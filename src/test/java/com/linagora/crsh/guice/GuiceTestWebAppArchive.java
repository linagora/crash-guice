package com.linagora.crsh.guice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.telnet.TelnetPlugin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;

import com.google.common.io.Resources;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceTestWebAppArchive {

	private static String getCRaSHMavenCoordinate() throws IOException {
		return "org.crsh:crsh.shell.core:" + getCRaSHVersion();
	}
	
	private static String getCRaSHVersion() throws IOException {
		Properties props = new Properties();
		InputStream in = CRaSHPlugin.class.getClassLoader().getResourceAsStream("META-INF/maven/org.crsh/crsh.shell.core/pom.properties");
		props.load(in);
		return props.getProperty("version");
	}
	
	public static WebArchive buildInstance() throws IllegalArgumentException, IllegalStateException, ResolutionException, CoordinateParseException, IOException {
		WebArchive archive = ShrinkWrap.create(WebArchive.class)
				.addClass(GuiceApplication.class)
				.addClass(SampleService.class)
				.addClass(SampleServlet.class)
				.addClass(CrashGuiceSupport.class)
				.addClass(CrashGuiceSupport.InjectorHolder.class)
				.addClass(GuiceServletContextListener.class)
				.addClass(TelnetPlugin.class)
				.addAsLibraries(Maven.resolver().addDependencies(
						MavenDependencies.createDependency(getCRaSHMavenCoordinate(), ScopeType.COMPILE, false))
						.resolve().withTransitivity().asFile())
				.addAsResource(GuiceTestWebAppArchive.class.getClassLoader().getResource("crash/commands/guice/"), "/crash/commands/guice/")
				.setWebXML(Resources.getResource("web.xml"));
		return archive;
	}
	
}
