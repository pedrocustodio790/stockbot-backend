FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia o código fonte
COPY . .

# Builda a aplicação e depois roda
RUN ./mvnw clean package -DskipTests

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "target/BackEnd_Refatore-0.0.1-SNAPSHOT.jar"]