package com.linagora.crsh.guice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginDiscovery;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

public class CrashGuiceSupport extends AbstractModule {

	public static class InjectorHolder extends PluginLifeCycle {

		private final Injector injector;
		private ClassLoader loader;
		private final CrashGuiceConfiguration configuration;

		@Inject
		public InjectorHolder(Injector injector, PluginDiscovery pluginDiscovery, CrashGuiceConfiguration configuration) throws IOException, URISyntaxException {
			this.injector = injector;
			this.loader = getClass().getClassLoader();
			this.configuration = configuration;
			FS cmdFS = createCommandFS();
			FS confFS = createConfFS();

			PluginContext context = new PluginContext(
					pluginDiscovery,
					buildGuiceMap(),
					cmdFS,
					confFS,
					loader);

			for (Map.Entry<PropertyDescriptor<Object>, Object> property: configuration.toEntries()) {
				context.setProperty(property.getKey(), property.getValue());
			}
			
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

	public CrashGuiceSupport() {
	}
	
	@Override
	protected void configure() {
		install(new CrashPluginsModule());
		bind(InjectorHolder.class).asEagerSingleton();
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
				pluginBinder.addBinding().toInstance(plugin);
				bind((Class<CRaSHPlugin>)plugin.getClass()).toInstance(plugin);
			}
		}
	}
	
}
