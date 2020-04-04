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

import io.bootique.BQRuntime;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.jdbc.test.junit5.TestDataManager;
import io.bootique.mybatis.testmappers2.T2Mapper;
import io.bootique.test.junit5.BQTestClassFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MybatisModuleTxIT {

    @RegisterExtension
    public static BQTestClassFactory testFactory = new BQTestClassFactory();

    private static SqlSessionManager sessionManager;
    private static Table t2;

    @RegisterExtension
    public TestDataManager dataManager = new TestDataManager(true, t2);

    @BeforeAll
    public static void setupDB() {
        BQRuntime runtime = testFactory
                .app("--config=classpath:io/bootique/mybatis/MybatisModuleTxIT.yml")
                .autoLoadModules()
                .module(b -> MybatisModule.extend(b).addMapper(T2Mapper.class))
                .createRuntime();
        DatabaseChannel channel = DatabaseChannel.get(runtime);
        t2 = createT2(channel);
        sessionManager = runtime.getInstance(SqlSessionManager.class);
    }

    private static Table createT2(DatabaseChannel channel) {
        channel.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" VARCHAR(10))");
        return channel.newTable("t2").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
    }

    @Test
    public void testCommit() {

        try (SqlSession session = sessionManager.openSession()) {
            T2Mapper mapper = session.getMapper(T2Mapper.class);
            mapper.insert(1L, "one");
            session.commit();
        }

        t2.matcher().eq("c1", 1L).assertOneMatch();
    }


    @Test
    public void testMapperOutsideSession_ImplicitAutocommit() {

        T2Mapper mapper = sessionManager.getMapper(T2Mapper.class);
        mapper.insert(1L, "one");
        t2.matcher().eq("c1", 1L).assertOneMatch();
    }

    @Test
    public void testRollback() {

        try (SqlSession session = sessionManager.openSession()) {
            T2Mapper mapper = session.getMapper(T2Mapper.class);
            mapper.insert(1L, "one");
            session.rollback();
        }

        t2.matcher().eq("c1", 1L).assertNoMatches();
    }

    @Test
    public void testNoCommit() {

        try (SqlSession session = sessionManager.openSession()) {
            T2Mapper mapper = session.getMapper(T2Mapper.class);
            mapper.insert(1L, "one");
        }

        t2.matcher().eq("c1", 1L).assertNoMatches();
    }

    @Test
    public void testCommitMultiple() {

        try (SqlSession session = sessionManager.openSession()) {
            T2Mapper mapper = session.getMapper(T2Mapper.class);
            mapper.insert(1L, "one");
            mapper.insert(2L, "two");
            session.commit();
        }

        t2.matcher().eq("c1", 1L).assertOneMatch();
        t2.matcher().eq("c1", 2L).assertOneMatch();
    }
}
