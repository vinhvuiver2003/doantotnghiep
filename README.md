# E-commerce Application

This is a full-stack e-commerce application built with Spring Boot and React.

## Deployment on Railway

### Prerequisites
- Railway account
- Git
- Docker (optional, for local testing)

### Steps to Deploy

1. Push your code to a GitHub repository

2. Install Railway CLI:
```bash
npm i -g @railway/cli
```

3. Login to Railway:
```bash
railway login
```

4. Initialize Railway project:
```bash
railway init
```

5. Link your project to Railway:
```bash
railway link
```

6. Add environment variables in Railway dashboard:
- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- SPRING_JPA_HIBERNATE_DDL_AUTO=update

7. Deploy your application:
```bash
railway up
```

### Local Development

1. Start the backend:
```bash
cd backend
mvn spring-boot:run
```

2. Start the frontend:
```bash
cd frontend
npm install
npm start
```

### Environment Variables

Create a `.env` file in the root directory with the following variables:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/your_database
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

### Docker Deployment

1. Build and run with Docker Compose:
```bash
docker-compose up --build
```

2. Access the application:
- Frontend: http://localhost:80
- Backend API: http://localhost:8080 