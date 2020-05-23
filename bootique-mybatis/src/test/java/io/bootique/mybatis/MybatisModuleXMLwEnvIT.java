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
import io.bootique.Bootique;
import io.bootique.mybatis.testmappersxml1.T4Mapper;
import io.bootique.mybatis.testpojos.TO4;
import io.bootique.test.junit5.BQApp;
import io.bootique.test.junit5.BQTest;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class MybatisModuleXMLwEnvIT {

    @BQApp(skipRun = true)
    private static BQRuntime app = Bootique
            .app("--config=classpath:io/bootique/mybatis/MybatisModuleXMLwEnvIT.yml")
            .autoLoadModules()
            .createRuntime();

    static SqlSessionManager getSessionManager() {
        return app.getInstance(SqlSessionManager.class);
    }

    @Test
    public void testSqlSessionManager() {

        try (SqlSession session = getSessionManager().openSession()) {

            T4Mapper mapper = session.getMapper(T4Mapper.class);

            mapper.createT4();
            mapper.insertT4();

            Optional<TO4> miss = mapper.find(3L);
            assertFalse(miss.isPresent());

            Optional<TO4> hit = mapper.find(7L);
            assertTrue(hit.isPresent());
            assertEquals(7, hit.get().getC1());
            assertEquals("y", hit.get().getC2());
        }
    }
}
