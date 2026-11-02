# Build stage: compile the WAR with Maven (no local JDK/Maven needed)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Runtime stage: stock Tomcat 10.1 on Java 17, app deployed as the ROOT context
FROM tomcat:10.1-jre17-temurin
ARG GIT_SHA=unknown
ENV GIT_SHA=${GIT_SHA}
RUN rm -rf /usr/local/tomcat/webapps/* \
 && useradd --system --uid 10001 --home /usr/local/tomcat tomcat \
 && chown -R tomcat:tomcat /usr/local/tomcat
COPY --from=build --chown=tomcat:tomcat /src/target/brightpath.war /usr/local/tomcat/webapps/ROOT.war
USER tomcat
EXPOSE 8080
