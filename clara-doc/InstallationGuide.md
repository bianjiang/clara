#Clone the repo
Use either Git GUI tools like SourceTree or command line tools like Git Bash to clone the repo.  The following instruction is based on command line tools:
```
cd {work directory}
git clone https://github.com/bianjiang/clara.git
```

#Set up environment variables on server
Before we start, make sure Java 1.7, the latest version of Maven and Tomcat 7.X server are installed.

##Configure database
The database configure file is located at: clara/clara-webapp/src/main/resources/META-INF/spring/clara/database.claradb.clara.properties
```
database.username={dbUsername}
database.password={password}
database.url={dbURL}
database.driverClassName={dbDriverClassName}
database.minPoolSize=50
database.maxPoolSize=100

hibernate.hbm2dd.auto=update
```

##Configure application properties
The application properties file is located at: clara/clara-webapp/src/main/resources/META-INF/spring/clara/application.clara.properties
```
application.app_name=CLARA
application.host={hostName}

application.base.protocol.identifier=0
application.base.contract.identifier=0


fileserver.upload.url={fileServerUploadURL}

fileserver.url={fileServerURL}

fileserver.host={fileServerHostName}
fileserver.local.dir.path=/Data/upload
fileserver.remote.dir.path=/data/upload
fileserver.sftp.user={sftpUsername}
fileserver.sftp.password={sftpUserPassword}
```

##Configure LDAP properties
The LDAP properties file is located at: clara/clara-webapp/src/main/resources/META-INF/spring/ldap.properties
```
ldap.enabled=true
ldap.provider-url={ldapProvider}
ldap.user-search-base={searchBase}
ldap.user-search-filter={searchFilter}
ldap.manager-dn={domainName}
ldap.manager-password={password}
```

##Set up environment variables
```
export JAVA_HOME={Java installation directory}
export PATH=$JAVA_HOME/bin:$PATH

export M2_HOME={Maven installation directory}
export M2=$M2_HOME/bin
export PATH=$M2:$PATH
```

Type in ```java -version``` and ```mvn -version``` to check if both are set correctly.  

##Install JDBC driver
Starting hibernate 4.18, it requires to use a jdbc4 compliant driver, which jtds doesn’t support it.

So, we switch the driver to sqljdbc4.jar, and upgraded the connection pool (c3p0) to a jdbc4 compliant version.

Microsoft has licenses issues with Maven, so sqljdbc4.jar is not in any of the maven repositories. So, we have to manually install the driver into local maven repository.

```
cd clara/lib
mvn install:install-file -Dfile=sqljdbc4.jar -Dpackaging=jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=4.0
```

#Build CLARA
```
cd {work directory}/clara
git pull
mvn clean package –DskipTests
{Tomcat installation directory}/bin/shutdown.sh
rm –Rf {Tomcat installation directory}/webapps/clara-webapp/
cd {work directory}/clara/clara-webapp/target/
mv clara-webapp-1.0.-SNAPSHOT/ {Tomcat work directory}/webapps/clara-webapp
{Tomcat installation directory}/bin/startup.sh
```

The system will be avaiable at `http://{host name}/clara-webapp`, e.g., [CLARA](https://clara.uams.edu/) at UAMS.


