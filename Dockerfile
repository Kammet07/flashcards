FROM openjdk:8-jdk
EXPOSE 8080:8080
RUN mkdir /app
COPY build/install/flashcards /app/
COPY .env /app/
COPY frontend-angular/dist /app/frontend-angular/dist/
WORKDIR /app/
CMD ["bin/flashcards"]