# Spring Boot Magomez Demo API

Este proyecto es una API REST desarrollada en **Spring Boot 3 / Java 17**, pensada para una prueba técnica de backend. 
Permite gestionar cuentas, tarjetas, transacciones y transferencias. 
La API se puede ejecutar tanto **localmente**  como en **Docker**

---

## Requisitos previos

Antes de ejecutar la aplicación, asegúrate de tener instalado:

- **Java 17 JDK** (OpenJDK o Oracle JDK)
- **Maven 3.8+**
- Opcionalmente, **Docker Desktop** y **Docker Compose** si quieres usar contenedores
- Postman o navegador para probar endpoints.
- IntelliJ IDEA u otro IDE para desarrollo

---

## Ejecutar la API **sin Docker**

1. Clona el proyecto:

git clone https://github.com/tu_usuario/spring-boot-magomez-demo-next.git
cd spring-boot-magomez-demo-next

---

2. Construye el proyecto con maven

mvn clean install

---

3. Ejecuta la aplicación
   
mvn spring-boot:run

La API se ejecutará en http://localhost:8080
Swagger UI estará disponible en: http://localhost:8080/swagger-ui.html

---

4. Para probar los endpoints:

Activar tarjeta:
POST http://localhost:8080/card/{id}/activate

Cambiar PIN:
POST http://localhost:8080/card/{id}/change-pin

Consultar movimientos:
GET http://localhost:8080/account/{id}/transactions

{id} = id de la tarjeta.
Retiros, depósitos y transferencias: consulta Swagger para los detalles de cada endpoint.

---

5. Ejecutar tests unitarios e integración:

   mvn test



## Ejecutar la API **con Docker**

1. Desde la raíz del proyecto (donde está el Dockerfile):

docker build -t spring-boot-magomez-demo-next (o el nombre que le quieras poner a la imagen de docker)

---

2. Ejecutar el contenedor
   
docker run -p 8080:8080 spring-boot-magomez-demo-next

---

3. docker-compose.yml:

docker-compose up --build


Levanta automáticamente la aplicación y servicios adicionales si se configuran.

---

4. Para detener todo:

 docker-compose down
