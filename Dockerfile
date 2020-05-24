FROM openjdk:10-jre
COPY build/libs/nominatim-ac-0.1.0.jar /opt/nominatim-ac/nominatim-ac-0.1.0.jar
CMD ["java","-jar","/opt/nominatim-ac/nominatim-ac-0.1.0.jar", "--spring.config.location=/opt/nominatim-ac/conf/application.properties"]
