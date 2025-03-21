/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.mybatis;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.resource.ResourceFactory;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

@BQConfig
public class SqlSessionManagerFactory {

    private final Logger logger = LoggerFactory.getLogger(SqlSessionManagerFactory.class);

    private final DataSourceFactory dsFactory;
    private final Provider<TransactionFactory> transactionFactory;
    private final @ByMybatisModule Set<Class<?>> mappers;
    private final @ByMybatisModule Set<Package> mapperPackages;
    private final Set<TypeHandler> typeHandlers;
    private final @TypeHandlerPackageByMybatisModule Set<Package> typeHandlerPackages;

    private String environmentId;
    private String datasource;
    private ResourceFactory config;

    @Inject
    public SqlSessionManagerFactory(
            DataSourceFactory dsFactory,
            Provider<TransactionFactory> transactionFactory,
            @ByMybatisModule Set<Class<?>> mappers,
            @ByMybatisModule Set<Package> mapperPackages,
            Set<TypeHandler> typeHandlers,
            @TypeHandlerPackageByMybatisModule Set<Package> typeHandlerPackages) {

        this.dsFactory = dsFactory;
        this.transactionFactory = transactionFactory;
        this.mappers = mappers;
        this.mapperPackages = mapperPackages;
        this.typeHandlers = typeHandlers;
        this.typeHandlerPackages = typeHandlerPackages;
    }

    public SqlSessionManager createSessionManager() {

        Configuration configuration = config != null
                ? createConfigurationFromXML()
                : createConfigurationFromScratch();

        // must install handlers before loading mappers... mappers need handlers
        mergeDITypeHandlers(configuration, typeHandlers, typeHandlerPackages);
        mergeDIMappers(configuration, mappers, mapperPackages);

        SqlSessionFactory sessionFactoryDelegate = new SqlSessionFactoryBuilder().build(configuration);
        return SqlSessionManager.newInstance(sessionFactoryDelegate);
    }

    protected Configuration createConfigurationFromXML() {

        String environmentId = getEnvironmentId();
        Configuration configuration = loadConfigurationFromXML(config, environmentId);

        // if no environment was present or matched during XML parsing, provide the one configured in Bootique
        if (configuration.getEnvironment() == null) {

            logger.debug("MyBatis XML configuration does not specify environment for '{}'. Bootstrapping environment from Bootique...", environmentId);

            // deferring TransactionFactory creation until we know for sure that we need it...
            Environment environment = createEnvironment();
            configuration.setEnvironment(environment);
        }

        return configuration;
    }

    protected Configuration createConfigurationFromScratch() {

        Environment environment = createEnvironment();
        return new Configuration(environment);
    }

    protected Environment createEnvironment() {

        String datasourceName = dataSourceName();
        logger.debug("Using Bootique DataSource named '{}' for MyBatis", datasourceName);

        DataSource ds = dsFactory.forName(datasourceName);
        return new Environment(getEnvironmentId(), transactionFactory.get(), ds);
    }

    protected Configuration loadConfigurationFromXML(ResourceFactory configResource, String environmentId) {

        URL configUrl = config.getUrl();
        logger.debug("Loading MyBatis configuration from XML at '{}' and environment '{}'", configUrl, environmentId);

        try (Reader reader = new InputStreamReader(configResource.getUrl().openStream(), "UTF-8")) {

            // "environmentId" filters an environment out of multiple choices.
            // TODO:  pass properties from YAML as 3rd arg
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environmentId);
            return parser.parse();
        } catch (IOException e) {
            throw new RuntimeException("Error reading MyBatis config from " + configUrl, e);
        }
    }

    protected void mergeDITypeHandlers(
            Configuration configuration,
            Set<TypeHandler> typeHandlers,
            Set<Package> typeHandlerPackages) {

        TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
        typeHandlers.forEach(registry::register);
        typeHandlerPackages.forEach(hp -> registry.register(hp.getName()));
    }

    protected void mergeDIMappers(
            Configuration configuration,
            Set<Class<?>> mappers,
            Set<Package> mapperPackages) {
        mappers.forEach(configuration::addMapper);
        mapperPackages.forEach(mp -> configuration.addMappers(mp.getName()));
    }

    protected String dataSourceName() {

        if (datasource != null) {
            return datasource;
        }

        Collection<String> names = dsFactory.allNames();
        if (names.size() == 1) {
            return names.iterator().next();
        }

        if (names.isEmpty()) {
            throw new IllegalStateException("No DataSources are available to MyBatis. Configure a DataSource via 'bootique-jdbc'");
        } else {
            throw new IllegalStateException("Can't map MyBatis DataSource: 'mybatis.datasource' is missing and more than one DataSource is available: " + names);
        }
    }

    protected String getEnvironmentId() {
        return environmentId != null ? environmentId : "development";
    }

    @BQConfigProperty("An optional MyBatis environment id. Default is 'development'")
    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    @BQConfigProperty("An optional name of the DataSource to use with MyBatis. A DataSource with the matching name " +
            "must be defined in 'bootique-jdbc' configuration. If missing, a default DataSource from 'bootique-jdbc' " +
            "is used.")
    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    @BQConfigProperty("An optional resource URL of an XML config file. Most of the things in MyBatis can be configured " +
            "via Bootique, but if you prefer XML configuration, this is the way to specify it. " +
            " If the XML contains <environment> tag, XML-provided DataSource will be used instead of the one from Bootique.")
    public void setConfig(ResourceFactory config) {
        this.config = config;
    }
}
