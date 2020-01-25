<!--
  Licensed to ObjectStyle LLC under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ObjectStyle LLC licenses
  this file to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->

[![Build Status](https://travis-ci.org/bootique/bootique-mybatis.svg)](https://travis-ci.org/bootique/bootique-mybatis)
[![Maven Central](https://img.shields.io/maven-central/v/io.bootique.mybatis/bootique-mybatis.svg?colorB=brightgreen)](https://search.maven.org/artifact/io.bootique.mybatis/bootique-mybatis/)

# bootique-mybatis

Provides [MyBatis](https://mybatis.org/mybatis-3/) integration with [Bootique](https://bootique.io).

*For additional help/questions about this example send a message to
[Bootique forum](https://groups.google.com/forum/#!forum/bootique-user).*

# Setup

## Add `bootique-mybatis` to your build

Add the dependency on `bootique-mybatis` to your build. Here is a Maven example:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>X.X</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
...
<dependency>
    <groupId>io.bootique.mybatis</groupId>
    <artifactId>bootique-mybatis</artifactId>
</dependency>
```
After that you can either configure your mappers in the code and use Bootique-provided DataSource, or use MyBatis XML
configuration. Below are a the examples of both.

## Mappers Configured in the Code / DataSource Provided by Bootique

Configure MyBatis mappers:
```java
public class MyModule implements Module {

	public void configure(Binder binder) {

        // add annotated Mappers ...
        MybatisModule.extend(binder)
            // ... a whole package of Mappers
        	.addMapperPackage(MyMapper1.class.getPackage())
            // ... a single mapper
            .addMapper(MyMapper2.class))
    }
}
```

Configure DataSource in Bootique:

```yaml
# Implicit single DataSource. MyBatis will find and use it automatically.
jdbc:
  myds:
    jdbcUrl: "jdbc:mysql://127.0.0.1:3306/mydb"
    username: root
    password: secret
```

```yaml
jdbc:
  myds:
    jdbcUrl: "jdbc:mysql://127.0.0.1/mydb"
    username: root
    password: secret

# Explicitly reference a named DataSource
mybatis:
  datasource: myds
```

## Everything is Configured in MyBatis XML

If you'd rather prefer to use MyBatis "canonical" approach with an XML config file, you can still do that (and combine
such approach with Bootique-configured DataSource if needed).

First, configure a reference to MyBatis YAML file:
```yaml
mybatis:
  environmentId: qa
  config: classpath:mybatis-config.xml
```

Second create MyBatis config XML as you normally would. In this example it contains the `<environment>..</environment>`
section with DB connection info. If you omit the "environment" config, make sure you configure a Bootique
DataSource in YAML as described above.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <environments default="default">

        <!-- If "environment" is not provided, Bootique will look for DataSource configuration in YAML -->
        <environment id="qa">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://127.0.0.1/mydb"/>
                <property name="username" value="root"/>
                <property name="password" value="secret"/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="com/foo/MyMapper1.xml"/>
        <mapper resource="com/foo/MyMapper2.xml"/>
    </mappers>
</configuration>
```

## Use MyBatis

Regardless of how MyBatis was configured, you can use it in the same way, by injecting `SqlSessionManager`:

```java
public class MyClass {

   @Inject
   private SqlSessionManager sessionManager;

   public void doSomething() {
      try (SqlSession session = sessionManager.openSession()) {
		MyMapper2 mapper = session.getMapper(MyMapper2.class);
		Optional<O1> o1 = mapper.find(1);
      }
   }
}
```