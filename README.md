# REGIONAlert
(former AlertCity)
An alert city system

Please, configure:
- Border database physical directory at:
-- br.com.meslin.alert.servlet.GetRegion.java
-- parameters:
--- -f RioDeJaneiro.lista 
--- -w /media/meslin/4E7E313D7E311EE1/Users/meslin/Google Drive/workspace-desktop-ubuntu/RegionAlert/Bairros/
- InterSCity IP address and TCP port at:
-- WEB-INF/web.xml
-- br.com.meslin.alert.connection.Constants.java
-- parameter -i VM005 

Capabilities are created (only) when the web server starts by br.com.meslin.alert.servlet.listener.CheckInterSCity

Environment
- Java 8
- Apache Tomcat 8.5
