# Используем официальный образ Maven на базе JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /AutomaticRegistrationWithBot

# Копируем файл настроек проекта (pom.xml) и загружаем зависимости
COPY pom.xml .

# Загружаем все зависимости, указанные в pom.xml
RUN mvn dependency:go-offline -B

# Копируем исходный код проекта
COPY src ./src

EXPOSE 8080
# Собираем проект
RUN mvn clean package -DskipTests

# Указываем базовый образ для конечного контейнера с Java
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /AutomaticRegistrationWithBot

# Устанавливаем зависимости для Chrome
RUN apk --no-cache add \
    bash \
    curl \
    chromium \
    chromium-chromedriver \
    nss \
    freetype \
    ttf-dejavu \
    fontconfig

# Копируем собранный JAR-файл из предыдущего шага
COPY --from=build /AutomaticRegistrationWithBot/target/demo-0.0.1-SNAPSHOT.jar ./demo-0.0.1-SNAPSHOT.jar

# Устанавливаем переменные окружения для запуска браузера в безголовом режиме
ENV DISPLAY=:99
ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROMEDRIVER_BIN=/usr/bin/chromedriver

# Открываем порт 8080
EXPOSE 8080

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "./demo-0.0.1-SNAPSHOT.jar"]
