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

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import jakarta.inject.Singleton;

public class MybatisModule implements BQModule {

    private static final String CONFIG_PREFIX = "mybatis";

    /**
     * Returns an instance of {@link MyBatisModuleExtender} used by downstream modules to load custom extensions of
     * services declared in the MybatisModule (mappers, etc). Should be invoked from a downstream Module's "configure"
     * method.
     *
     * @param binder DI binder passed to the Module that invokes this method.
     * @return an instance of {@link MyBatisModuleExtender} that can be used to load MyBatis custom extensions.
     */
    public static MyBatisModuleExtender extend(Binder binder) {
        return new MyBatisModuleExtender(binder);
    }

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Integrates Mybatis persistence library")
                .config(CONFIG_PREFIX, SqlSessionManagerFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
        MybatisModule.extend(binder).initAllExtensions();
    }

    @Provides
    @Singleton
    public SqlSessionFactory provideSessionFactory(SqlSessionManager sessionManager) {
        return sessionManager;
    }

    @Provides
    @Singleton
    TransactionFactory provideTransactionFactory() {
        return new JdbcTransactionFactory();
    }

    // SqlSessionManager is a newer more feature-rich version of SqlSessionFactory (which actually wraps a factory)
    @Provides
    @Singleton
    public SqlSessionManager provideSessionManager(ConfigurationFactory configFactory) {

        return configFactory
                .config(SqlSessionManagerFactory.class, CONFIG_PREFIX)
                .createSessionManager();
    }
}
