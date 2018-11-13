FROM openjdk:10-jre
COPY build/libs/gs-spring-boot-0.1.0.jar /opt/nominatim-ac/gs-spring-boot-0.1.0.jar
CMD ["java","-jar","/opt/nominatim-ac/gs-spring-boot-0.1.0.jar", "--spring.config.location=/opt/nominatim-ac/conf/application.properties"]
