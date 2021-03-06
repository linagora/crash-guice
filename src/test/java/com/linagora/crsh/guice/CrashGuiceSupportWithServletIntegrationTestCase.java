package com.linagora.crsh.guice;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CrashGuiceSupportWithServletIntegrationTestCase {

	private TelnetClient telnetClient;


	@Before
	public void setup() {
		telnetClient = new TelnetClient();
	}

	@After
	public void teardown() throws IOException {
		telnetClient.disconnect();
	}
	
	@Test
	@RunAsClient
	public void testCrashPrintPropertyAfterAJump(@ArquillianResource URL baseURL) throws IOException {
		int telnetPort = Integer.valueOf(Request.Options(baseURL.toExternalForm()).execute().returnContent().asString());
		Request.Get(baseURL.toExternalForm() + "?howHigh=5").execute().discardContent();
		
		telnetClient.connect("localhost", telnetPort);

		InputStream in = telnetClient.getInputStream();
		PrintStream out = new PrintStream(telnetClient.getOutputStream());
		TelnetHelper.readUntil("% ", in);
		out.println("guice print -p counter com.linagora.crsh.guice.SampleService");
		out.flush();
		String response = TelnetHelper.readUntil("% ", in);
		assertThat(response).contains("5");
		assertThat(response).contains("--");
		assertThat(response).contains("1");
	}
	

	@Deployment
	public static WebArchive createDeployment() throws IllegalArgumentException, IllegalStateException, ResolutionException, CoordinateParseException, IOException {
		return GuiceTestWebAppArchive.buildInstance();
	}
}
