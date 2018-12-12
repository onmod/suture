package net.dloud.platform.parse.source;


import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class which constructs an {@link Jdbi} instance which can conveniently
 * participate in Spring's transaction management system.
 */
public class JdbiFactoryBean extends AbstractFactoryBean<Jdbi> {
    private final Map<String, Object> globalDefines = new HashMap<>();
    private DataSource dataSource;
    private boolean autoInstallPlugins = false;
    private Collection<JdbiPlugin> plugins = Collections.emptyList();

    public JdbiFactoryBean() {
    }

    public JdbiFactoryBean(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected Jdbi createInstance() throws Exception {
        final Jdbi jdbi = Jdbi.create(() -> DataSourceUtils.getConnection(dataSource));

        if (autoInstallPlugins) {
            jdbi.installPlugins();
        }

        plugins.forEach(jdbi::installPlugin);

        globalDefines.forEach(jdbi::define);

        return jdbi;
    }

    /**
     * See {@link org.springframework.beans.factory.FactoryBean#getObjectType}
     */
    @Override
    public Class<Jdbi> getObjectType() {
        return Jdbi.class;
    }

    /**
     * The datasource, which should be managed by spring's transaction system, from which
     * the {@link Jdbi} will obtain connections
     *
     * @param dataSource the data source.
     * @return this
     */
    public JdbiFactoryBean setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    /**
     * Installs the given plugins which will be installed into the {@link Jdbi}.
     *
     * @param plugins collection of Jdbi plugins to install.
     * @return this
     */
    @Autowired(required = false)
    public JdbiFactoryBean setPlugins(Collection<JdbiPlugin> plugins) {
        this.plugins = new ArrayList<>(plugins);
        return this;
    }

    /**
     * Sets whether to install plugins automatically from the classpath, using
     * {@link java.util.ServiceLoader} manifests.
     *
     * @param autoInstallPlugins whether to install plugins automatically from
     *                           the classpath.
     * @return this
     * @see Jdbi#installPlugins() for detail
     */
    public JdbiFactoryBean setAutoInstallPlugins(boolean autoInstallPlugins) {
        this.autoInstallPlugins = autoInstallPlugins;
        return this;
    }

    public void setGlobalDefines(Map<String, Object> defines) {
        globalDefines.putAll(defines);
    }

    /**
     * Verifies that a dataSource has been set
     */
    @Override
    @SuppressWarnings("unused")
    public void afterPropertiesSet() throws Exception {
        if (dataSource == null) {
            throw new IllegalStateException("'dataSource' property must be set");
        }
        super.afterPropertiesSet();
    }
}