(kill -9 $(lsof -ti :8080)) &(kill -9 $(lsof -ti :5173))

(cd oikos-api/ && ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005") & (cd oikos-web/ && npm run dev) &