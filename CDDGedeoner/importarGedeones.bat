@ECHO OFF
cd C:\Users\pedro.dulce\git\pcm\CDDGedeoner\target\gedeoner\WEB-INF\classes
java -classpath .;"..\lib\*" gedeoner.utils.ImportarTareasGEDEON C:\\exports\\GEDEON C:\\Users\\99GU3997\\apache-tomcat-9.0.52\\data\\sqlite factUTEDBlite.db
cd C:\Users\pedro.dulce\git\pcm\CDDGedeoner
ECHO fin