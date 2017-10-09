package com.linagora.crsh.guice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.crsh.auth.AuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginDiscovery;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class CrashGuiceSupport extends AbstractModule {

	private static final String AUTOSTART = "autostart";

	public static class Bootstrap extends PluginLifeCycle {

		private final Injector injector;
		private ClassLoader loader;
		private final CrashGuiceConfiguration configuration;
		private PluginContext context;

		@Inject
		public Bootstrap(Injector injector, PluginDiscovery pluginDiscovery, CrashGuiceConfiguration configuration,
				@Named(AUTOSTART)Boolean autostart) throws IOException, URISyntaxException {
			this.injector = injector;
			this.loader = getClass().getClassLoader();
			this.configuration = configuration;
			FS cmdFS = createCommandFS();
			FS confFS = createConfFS();

			context = new PluginContext(
					pluginDiscovery,
					buildGuiceMap(),
					cmdFS,
					confFS,
					loader);

			for (Entry<PropertyDescriptor<List>, List> property: configuration.listsAsEntries()) {
				context.setProperty(property.getKey(), property.getValue());
			}
			for (Entry<PropertyDescriptor<Integer>, Integer> property: configuration.integersAsEntries()) {
				context.setProperty(property.getKey(), property.getValue());
			}
			
			if (autostart) {
				start();
			}
		}

		public void start() {
			context.refresh();
			start(context);
		}
		
		private Map<String, Object> buildGuiceMap() {
			return ImmutableMap.of(
					"factory", injector,
					"properties", configuration,
					"beans", new GuiceMap(injector)
					);
		}

		protected FS createCommandFS() throws IOException, URISyntaxException {
			FS cmdFS = new FS();
			cmdFS.mount(loader, Path.get("/crash/commands/"));
			cmdFS.mount(loader, Path.get("/crash/commands/guice/"));
			return cmdFS;
		}

		protected FS createConfFS() throws IOException, URISyntaxException {
			FS confFS = new FS();
			confFS.mount(loader, Path.get("/crash/"));
			return confFS;
		}
		
		public void destroy() throws Exception {
			stop();
		}
	}

	private final boolean autostart;
	
	public CrashGuiceSupport(boolean autostart) {
		this.autostart = autostart;
	}

	public CrashGuiceSupport() {
		this(true);
	}

	
	@Override
	protected void configure() {
		install(new CrashPluginsModule());
		bind(Boolean.class).annotatedWith(Names.named(AUTOSTART)).toInstance(autostart);
		bind(Bootstrap.class).asEagerSingleton();
	}

	private static class CrashPluginsModule extends AbstractModule {
		
		@Override
		protected void configure() {
			ClassLoader loader = getClass().getClassLoader();
			PluginDiscovery discovery = new ServiceLoaderDiscovery(loader);
			
			Multibinder<CRaSHPlugin<?>> pluginBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<CRaSHPlugin<?>>(){});
			
			Iterable<CRaSHPlugin<?>> plugins = discovery.getPlugins();
			bind(PluginDiscovery.class).to(GuicePluginDiscovery.class);
			for (CRaSHPlugin<?> plugin: plugins) {
				if (!isAuthenticationPlugin(plugin)) {
					pluginBinder.addBinding().toInstance(plugin);
					bind((Class<CRaSHPlugin>)plugin.getClass()).toInstance(plugin);
				}
			}
		}

		private boolean isAuthenticationPlugin(CRaSHPlugin<?> plugin) {
			if (plugin.getType().isAssignableFrom(AuthenticationPlugin.class)) {
				return true;
			}
			if (ImmutableList.copyOf(plugin.getType().getInterfaces()).contains(AuthenticationPlugin.class)) {
				return true;
			}
			return false;
		}
	}
	
}
