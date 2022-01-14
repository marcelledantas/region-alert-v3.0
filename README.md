# REGIONAlert
##### (former AlertCity)
## An alert city system

[![MUSANet|Solid](https://raw.githubusercontent.com/meslin8752/InterSCity-onibus/master/PoweredByMUSANet.png)](https://musanet.meslin.com.br/)

This project is a alert manager.


### Running REGIONAlert:

To run REGIONAlert, you will need the [HumanMobility](https://github.com/meslin8752/HumanMobility.git) project to simulate users walking around the city.

Please, configure:
* Border database physical directory at:
  * br.com.meslin.alert.servlet.GetRegion.java
  * parameters:
    * (deprecated since 2022-01-11) -f Bairros/RioDeJaneiro.lista
    * (optional since 2022-01-11) -w /media/meslin/4E7E313D7E311EE1/Users/meslin/Google Drive/workspace-desktop-ubuntu/RegionAlert/
* InterSCity IP address and TCP port at:
  * WebContent/WEB-INF/web.xml
  * br.com.meslin.alert.connection.Constants.java (???)
  * parameter -i VM005
* Database at (address, user, and password):
  * br.com.meslin.alert.dao.UserDAO
    * CLASSNAME
    * DBURL

Capabilities are created by br.com.meslin.alert.servlet.listener.CheckInterSCity (only) when the web server starts

### Environment
* [Java 8](https://www.java.com/en/download/)
* [Apache Tomcat 8.5](https://tomcat.apache.org/download-80.cgi)
* [MySQL](https://dev.mysql.com/downloads/)

2022-01-11
* Fixed: get initial parameters from web
* -w command line parameter no longer needed (now, it's optional)
* -f command line parameter no longer needed
* all configuration parameters are configured in web.xml
* capability "uv" replaced by fake capability "fake" because new InterSCity version no longer has uv as a default configured capability

2022-01-07
* Database changed from MySQL to SQLite due to changes in MySQL package
