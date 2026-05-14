FROM eclipse-temurin:21.0.11_10-jre-noble
# Create ARG for Copy the file from jenkins
ARG JAR_FILE_PATH
# Create a directory
WORKDIR /togo_app
# Copy the jar from local
COPY ${JAR_FILE_PATH} service-one.jar
#Run the jar file on docker
ENTRYPOINT ["java", "-jar", "service-one.jar"]
