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
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Set;

@BQConfig
public class SqlSessionManagerFactory {

    private String environmentId;
    private String datasource;

    public SqlSessionManager createSessionManager(
            DataSourceFactory dataSourceFactory,
            TransactionFactory transactionFactory,
            Set<Class<?>> mappers,
            Set<Package> mapperPackages) {

        String datasourceName = dataSourceName(dataSourceFactory);
        DataSource ds = dataSourceFactory.forName(datasourceName);

        Environment env = new Environment(getEnvironmentId(), transactionFactory, ds);
        Configuration configuration = createConfiguration(env, mappers, mapperPackages);

        SqlSessionFactory sessionFactoryDelegate = new SqlSessionFactoryBuilder().build(configuration);
        return SqlSessionManager.newInstance(sessionFactoryDelegate);
    }

    protected Configuration createConfiguration(
            Environment env,
            Set<Class<?>> mappers,
            Set<Package> mapperPackages) {

        Configuration configuration = new Configuration(env);

        mappers.forEach(configuration::addMapper);
        mapperPackages.forEach(mp -> configuration.addMappers(mp.getName()));

        return configuration;
    }

    protected String dataSourceName(DataSourceFactory dataSourceFactory) {

        if (datasource != null) {
            return datasource;
        }

        Collection<String> names = dataSourceFactory.allNames();
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


}
