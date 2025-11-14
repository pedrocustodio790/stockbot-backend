# Use uma imagem oficial do Java
FROM eclipse-temurin:17-jdk-alpine

# Diretório de trabalho
WORKDIR /app

# Copie o arquivo JAR
COPY target/*.jar app.jar

# Exponha a porta
EXPOSE 10000

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]