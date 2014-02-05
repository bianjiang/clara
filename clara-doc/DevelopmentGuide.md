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
    * __**/WEB-INF/emailtemplate/*__: Apache Velocity Templates for all the emails;
    * __**/WEB-INF/layouts/*__: Page layouts;
    * __**/WEB_INF/tags/*__: Custom tags;
    * __**/WEB_INF/views/*__: Custom Web pages;
    * __**/static/images/*__: Images, icons and logos;
    * __**/static/js/*__: JavaScripts;
    * __**/static/styles/*__: Custom page styles;
    * __**/static/xml/*__: Static xml files including review checklists, committee list, form summary templates, queque templates, report templates, validation rules, workflow tempates, etc,.


### Database schemas:
All tables have id as *primary key*, concurrent_version as *version control number*, and retired as *record retired flag*.

* `dbo.agenda(agenda_date, irb_roster, meeting_xml_data)`: Stores agenda realted informations.
* `dbo.agenda_item(agenda_item_category, agenda_id, protocol_form_id, agenda_item_status)`: Stores agenda item related inforamtions.
	* `agenda_id`: mapped to `dbo.agenda.id`
	* `protocol_form_id`: mapped to `dbo.protocol_form.id`
* `dbo.agenda_item_reviewer(agenda_item_id, irb_reviewer_id)`: Map of irb reviewers to agenda items.
	* `agenda_item_id`: mapped to `dbo.agenda_item.id`
	* `irb_reviewer_id`: mapped to `dbo.irb_reviewer.id`
* `dbo.agenda_roster_memeber(reason, agenda_irb_reviewer_status, agenda_id, alternate_irb_reviewer_id, irb_reviewer_id)`: Map of irb roster memebers to each agenda.
	* `agenda_id`: mapped to `dbo.agenda.id`
	* `alternative_irb_reviewer_id`: mapped to `dbo.irb_reviewer.id`
	* `irb_reviewer_id`: mapped to `dbo.irb_reviewer.id`
* `dbo.agenda_status(agenda_status, modified, note, agenda_id, user_id)`: Stores Agenda statuses history.
	* `agenda_id`: mapped to `dbo.agenda.id`
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.audit(datetime, event_type, extra_data，message)`: Stores audit logs such as user login and logout log, budget modification logs, etc.
* `dbo.contract(created, meta_data_xml, contract_identifier)`: Stores contract information.
* `dbo.contract_form(contract_form_type, created, meta_data_xml, contract_id, parent_id)`: Stores contract form information, such as New Contract form and Contract Amendment form.
	* `contract_id`: mapped to `dbo.contract.id`
	* `parent_id`: mapped to `dbo.contract_form.id`
* `dbo.contract_form_committee_comment(comment_type, committee, modified, text, contract_form_id, reply_to_id, user_id, comment_status, in_letter, is_private)`: Stores comments made by review committees to individual contract form.
	* `contract_form_id`: mapped to `dbo.contract_form.id`
	* `reply_to_id`: mapped to `dbo.contract_form_committee_comment.id`
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.contract_form_committee_status(committee, modified, contract_form_committee_status, contract_form_id, note, user_id, caused_by_committee)`: Stores committee review status change history.
	* `contract_form_id`: mapped to `dbo.contract_form.id`
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.contract_form_status(modified, contract_form_status, contract_form_id, caused_by_committee, caused_by_user_id)`: Stores contract form status change history.
	* `contract_form_id`: mapped to `dbo.contract_form.id`
	* `caused_by_user_id`: mapped to `dbo.user_account.id`
* `dbo.contract_form_xml_data(contract_form_xml_data_type, created, xml_data, contract_form_id, parent_id)`: Stores answers to questions in each contract form.
	* `contract_form_id`: mapped to `dbo.contract_form.id`
	* `parent_id`: mapped to `dbo.contract_form_xml_data.id`
* `dbo.contract_form_xml_data_document(category, committee, created, title, contract_form_xml_data_id, parent_id, upload_file_id, user_id, version_id, status)`: Stores documents' information attached to individual contract form.
	* `contract_form_xml_data_id`: mapped to `dbo.contract_form_xml_data.id`
	* `parent_id`: mapped to `dbo.contract_form_xml_data_document.id`
	* `upload_file_id`: mapped to `dbo.upload_file.id`
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.contract_status(modified, contract_status, contract_id, caused_by_committee, caused_by_user_id)`: Stores contract status change history.
	* `contract_id`: mapped to `dbo.contract.id`
	* `caused_by_user_id`: mapped to `dbo.user_account.id`
* `dbo.email_template(identifier, send_to, cc, bcc, subject, vm_template, type)`: Stores all email templates informatioin.
	* `send_to`:  JSON string, such as `[{"address":"INDIVIDUAL_{emailAddress}","type":"INDIVIDUAL","desc":"{fullName}"}]`, `[{"address":"GROUP_realGatekeeper","type":"GROUP","desc":"Gatekeeper"}]`
	* `vm_template`: mapped to Velocity templates in `/WEB-INF/emailtemplate/`
* `dbo.irb_reviewer(is_affiliated, degree, irb_roster, type, comment, user_id, is_alternative_member, specialty, is_chair, is_expedited)`: Stores IRB reviewers' information.
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.message_post(expired_date, title, message, message_level)`: Stores messages posted on the login page.
* `dbo.mutex_lock(object_class, modified, object_id, user_id)`: Stores form and agenda lock information.
	* `user_id`: mapped to `dbo.user_account.id`
	* `object_class`: Object class name including package names.
* `dbo.person(department, email, firstname, job_title, lastname, middlename, sap, username, workphone, state, street_address, zip_code, annual_salary)`: Stores user's detailed information, which comes from either LDAP or user input.
* `dbo.protocol(created, meta_data_xml, protocol_identifier)`: Stores protocol information.
* `dbo.protocol_form(created, protocol_form_type, parent_id, protocol_id, meta_data_xml)`: Stores protocol forms' information of individual form, such as New Submission form, Continuing Review form, Modification form, etc.
	* `parent_id`: mapped to `dbo.protocol_form.id`
	* `protocol_id`: mapped to `dbo.protocol.id`
* `dbo.protocol_form_committee_comment(comment_type, committee, modified, text, protocol_form_id, reply_to_id, user_id, comment_status, in_letter, is_private)`: Stores comments made by review committees to individual protocol form.
	* `protocol_form_id`: mapped to `dbo.protocol_form.id`
	* `reply_to_id`: mapped to `dbo.protocol_form_committee_comment.id`
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.protocol_form_committee_status(committee, modified, protocol_form_committee_status, protocol_form_id, note, caused_by_committee, caused_by_user_id, xml_data, action)`: Stores committee review status change history.
	* `protocol_form_id`: mapped to `dbo.protocol_form.id`
	* `caused_by_use_id`: mapped to `dbo.user_account.id`
* `dbo.protocol_form_status(modified, protocol_form_status, protocol_form_id, note, caused_by_committee, caused_by_user_id)`: Stores protocol form status change history.
	* `protocol_form_id`: mapped to `dbo.protocol_form.id`
	* `caused_by_user_id`: mapped to `dbo.user_account.id`
* `dbo.protocol_form_user_element_template(created, name, template_type, xml_data, user_id)`: Stores user customized templates, such as budget template, staff template, etc.
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.protocol_form_xml_data(created, protocol_form_xml_data_type, xml_data, parent_id, protocol_form_id)`: Stores answers to questions in each protocol form.
	* `parent_id`: mapped to `dbo.protocol_form_xml_data.id`
	* `protocol_form_id`: mapped to `dbo.protocol_form.id`
* `dbo.protocol_form_xml_data_document(category, created, title, parent_id, protocol_form_xml_data_id, uploaded_file_id, user_id, committee, version_id, status)`: Stores documents' information attached to individual protocol form.
	* `parent_id`: mapped to `dbo.protocol_form_xml_data_document.id`
	* `protocol_form_xml_data_id`: mapped to `dbo.protocol_form_xml_data.id`
	* `uploaded_file_id`: mapped to `dbo.upload_file.id`
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.protocol_status(modified, protocol_status, protocol_id, note, caused_by_committee, caused_by_user_id)`: Stores protocol status change history.
	* `protocol_id`: mapped to `dbo.protocol.id`
	* `caused_by_user_id`: mapped to `dbo.user_account.id`
* `dbo.related_object(created, object_id, object_type, related_object_id, related_object_type)`: Stores map of relations between objects, such as related contract to protocol, etc.
* `dbo.report_criteria(report_id, criteria)`: Stores search criterias to individual report.
	* `report_id`: mapped to `dbo.report_template.id`
	* `criteria`: JSON String, generated from `/webapp/static/xml/report/fields.xml`
* `dbo.report_field(report_id, field)`: Stores display fields of individual report.
	* `report_id`: mapped to `dbo.report_template.id`
	* `field`: JSON String, such as `{"fieldIdentifier":"protocolstatus","fieldDisplayName":"Protocol Status","defaultDisplay":"true","order":"0","alias":"protocolStatus","value":""}`
* `dbo.report_template(type_description, description, created, parameters, user_id, status, global_operator, schedule_type)`: Stores report information, such as report name, schedule type, ect.
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.role(display_name, name, role_permission_identifier, committee, department_level, is_irb_roster, condition)`: Stores committee information.
* `dbo.role_default_permission(role_id, permission)`: Stores default permissions to each role.
	* `role_id`: mapped to `dbo.role.id`
* `dbo.search_bookmark(object_class, name, search_criterias, user_id)`: Stores user customized bookmark information.
	* `user_id`: mapped to `dbo.user_account.id`
	* `search_criterias`: JSON String, such as `[{"searchField":"PROTOCOL_STATUS","searchOperator":"EQUALS","keyword":"OPEN"}]`
* `dbo.securable_object(object_class, object_id, object_identification_expression, user_object_id_expression)`: Stores object securable information, which is used to check if the object is accessible or not.
* `dbo.securable_object_acl(owner_class, owner_id, permission, securable_object_id)`: Stores individule user's access permission to a securable object, which is used to check if the user have "WRITE" AND/OR "READ" permissions.
	* `owner_id`: mapped to `dbo.user_account.id`
	* `securable_object_id`: mapped to `dbo.securable_object.id`
* `dbo.track(modified, type, xml_data, ref_object_class, ref_object_id)`: Stores object change log.
* `dbo.uploaded_file(content_type, created, ext, filename, identifier, path, size)`: Stores uploaded file information.
* `dbo.user_account(account_nonexpired, account_nonlocked, credentials_nonexpired, password, user_type, username, person_id, is_trained, profile, cv_file_id, digital_signature_path)`: Stores CLARA user account information.
	* `person_id`: mapped to `dbo.person.id`
* `dbo.user_permission(user_id, permission)`: Stores permissions to individual user.
	* `user_id`: mapped to `dbo.user_account.id`
* `dbo.user_role(role_id, user_id, college, department, sub_department, irb_roster, is_delegate)`: Stores user's roles information.
	* `role_id`: mapped to `dbo.role.id`
	* `user_id`: mapped to `dbo.user_account.id`
	* `college`: maped to `dbo.college.id`
	* `department`: mapped to `dbo.department.id`
	* `sub_department`: mapped to `dbo.sub_department.id`


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
