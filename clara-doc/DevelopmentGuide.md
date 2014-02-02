How to setup development environment?
=====
The project is managed by [Apache Maven](http://maven.apache.org/). Since the applicaiton is written under Spring, the easiest way to start changing CLARA code is to import the project into the [Spring Tool Suite](http://www.eclipse.org/downloads/) (a Eclipse based J2EE IDE for Spring applications).

### Follow these steps to setup CLARA in STS:

0. Download and install STS from http://spring.io/tools/sts.
1. Clone the CLARA github repo

    ````
      git clone https://github.com/bianjiang/clara.git
    ````
    
2. Open STS, and import CLARA as an existing MAVEN project.
3. Maven will download all necessary third party libraries; however, SQL Server driver is not under a open-source license and cannot be downloaded from the central maven repository. So you will have to install sqljdbc4.jar separately.

    ```
    # under clara/lib directory; execute:
    mvn install:install-file -Dfile=sqljdbc4.jar -Dpackaging=jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=4.0
    ```
4. (Optional) You can setup Apache Tomcat inside STS and run the CLARA project within the IDE (See tutorial on [how to use Tomcat with Eclipse](http://www.coreservlets.com/Apache-Tomcat-Tutorial/tomcat-7-with-eclipse.html))

### Project folder structure

The project is split into two sub-components: `clara-core` and `clara-webapp`. `clara-core` contains mostly utility code, base classes for various domain objects and patches to Spring or hibernate. Most development activities will occur in `clara-webapp`.

##### clara-webapp:
    
* __src/main/java/*__: Java code (business logics, hibernate domains, DAOs, web controllers, services, etc.)
    * __src/main/java/edu/uams/clara/integration/*__: Integration code and interfaces with other systems such as Epic.
    * __src/**/scheduler/*__: Scheduled tasks such as sending daily email notification emails:
    * __src/**/webapp/*__: Main web backend components:
        * __src/**/webapp/admin/*__: Admin module (user management, permissibile value setups, IRB roaster management, etc.)
        * __src/**/webapp/fileserver/*__: File/document management
        * __src/**/webapp/protocol/*__: Protocol module
        * __src/**/webapp/contract/*__: Contract tracking module
        * __src/**/webapp/queue/*__: Review queues, live IRB meeting module server side.
        * __src/**/webapp/report/*__: Reporting module
        * __src/**/webapp/xml/*__: XML processing codes (validation, merging, diff, etc.)
* __src/main/resources/*__: property files (database connection string configurations, JPA's persistence.xml, application property configurations).
* __src/main/webapp/*__: Spring configuration files; html views, javascripts, css, etc.
    * __**/WEB-INF/spring/*__: Spring's XML configurations;
    * `TODO`

### Database schemas:

* `TODO`: List database tables

How form works in CALRA?
=====

* `TODO`: Explain how (XML) `forms` are stored, versioned, and organized in database.
* `TODO`: Explain how difference pieces of codes are glued together (VIEWs, Javascript widgets, Form Controllers)

How CLARA's workflow engine works?
=====

CLARA's workflow rules are encoded expressively in `XML` format (Take a close look at the workflow rules in __src/main/webapp/static/xml/workflow/**__) e.g.;

Also, see [clara-doc/Refs](/clara-doc/Refs) for diagrams of UAMS's clinical research administration workflows.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<business-object-status>
	<business-object type="PROTOCOL">
		<form type="NEW_SUBMISSION">
			<form-status value="" form-committee-status="ANY">
				<committee name="PI">
					<review-page page-name="">
						<actions>
							<action type="CREATE" condition="">
								<change-status>
									<status status="DRAFT" />									
									<form-status status="DRAFT" />
								</change-status>
								<logs>
									<log log-type="ACTION" event-type="NEW_PROTOCOL_CREATED" form-type="{FORM_TYPE}" form-id="{FORM_ID}" parent-form-id="{PARENT_FORM_ID}" action-user-id="{USER_ID}" actor="{USER_NAME}" timestamp="{NOW_TIMESTAMP}" date-time="{NOW_DATETIME}"><span class="history-log-message">New Submission has been created by {USER_WITH_EMAIL_LINK}.</span></log>
								</logs>
							</action>
						</actions>
					</review-page>
				</committee>
			</form-status>
		</form>
  </business-object>
</business-object-status>
```
