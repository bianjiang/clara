CLARA
=====

Clinical Research Administration (CLARA), developed at the University of Arkansas for Medical Sciences (UAMS), is a comprehensive web-based system that can streamline research administrative tasks such as submissions, reviews and approval processes for both investigators and different review committees on a single integrated platform. Even more importantly, CLARA not only helps investigators to meet regulatory requirements but also provides tools for managing other clinical research activities such as budgeting, contracting, and participant schedule planning.

Features
=====
Key CLARA features include:

* An IRB "Live Meeting" module
* An interface for multiple oversight review committees (IRB, Biohazard, and more)
* An adverse event reporting process
* A budget development interface and budget tracking module
* A clinical trial contract reviewing and tracking module
* Seamless interface with UAMS' open-source [Comprehensive Research Informatics Suite](http://tri.uams.edu/research-resources-services-directory/biomedical-informatics-data-services/clinical-trials-management/) for clinical trial management and participant tracking
* Interface with electronic medical record (EMR) and other hosptial information systems such as [Epic](www.epic.com/) to create research study definitions for tracking and billing research subjects.

Getting Started
=====
### Technology stack:
* Application development platform and frameworks: Java 1.7; [Spring](http://spring.io/) 3.x; JPA/[Hibernate](http://hibernate.org/); 
* Web development stack: [Sencha Ext JS](https://www.sencha.com/products/extjs/) 3.x/4.x; [jQuery](http://jquery.com/); [Bootstrap](http://getbootstrap.com/)
* Database engine: Microsoft SQL Server (2008)   
* Web container: Apache Tomcat [7](http://tomcat.apache.org/tomcat-7.0-doc/index.html)

### How to install?

1. Clone the repo:

    ```
    git clone https://github.com/bianjiang/clara.git
    ```
    
2. Compile with Maven `mvn`:
    
    ```
    mvn clean package -DskipTests
    ```

3. Config database, LDAP authentiction, etc:
    
    ```
    See ``Installation Guide`` for detailed configration.
    ```

4. Copy the `clar-webapp.war` into Tomcat's `webapps`; and start Tomcat.
5. The system will be avaliable at `http://localhost:8080/clara-webapp/` e.g., [CLARA](https://clara.uams.edu/) at UAMS.

See [Installation Guide](clara-doc/InstallationGuide.md) for detailed installation guide; and [Development Guide](clara-doc/DevelopmentGuide.md) for how to setup development environment and various other development resources.

Documentations
=====

* [CLARA User Manaul](https://clara.uams.edu/wiki/doku.php?id=start) (based on the installation at UAMS)
* [Installation Guide](clara-doc/InstallationGuide.md)
* [Development Guide](clara-doc/DevelopmentGuide.md) (Work in progress)

Various other documentations such as form templates, workflow diagrams; and development resources can be found at [clara-doc](clara-doc/)

Questions?
=====
If you have any questions, please feel free to contact Jiang Bian (Email: jbian at uams.edu).
