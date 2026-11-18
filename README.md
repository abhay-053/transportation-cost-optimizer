
# Transportation Cost Optimizer

A backend application built using Java and Spring Boot to optimize transportation costs by assigning transporters to delivery lanes.

The application uses Google OR-Tools CP-SAT to determine optimal transporter assignments while balancing transporter utilization and total transportation cost.

## Problem Overview

Given:
- A set of transportation lanes, each having an origin and destination.
- A set of transporters.
- Transportation cost quotes submitted by different transporters for different lanes.
- A maximum limit (K) on the number of transporters that can be selected.

The objective is to:
1. Maximize the number of distinct transporters used, up to K.
2. Minimize the total transportation cost while satisfying the first objective.
3. Assign exactly one eligible transporter to every lane.
4. Exclude transporters that have not submitted a quote for a particular lane.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend development |
| Spring Boot | REST API development |
| Spring Data JPA | Database operations |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| PostgreSQL | Persistent data storage |
| Flyway | Database schema migrations |
| Google OR-Tools CP-SAT | Optimization engine |
| Maven | Dependency and build management |
| Docker | Containerization |
| Springdoc OpenAPI | API documentation |
| JUnit | Automated testing |

## Architecture

The application follows a layered architecture:

- **Controller Layer:** Exposes REST APIs and handles incoming requests.
- **Service Layer:** Implements business logic and coordinates application workflows.
- **Repository Layer:** Handles database operations using Spring Data JPA.
- **Security Layer:** Handles authentication, JWT validation and authorization.
- **Optimization Layer:** Models and solves the transporter assignment problem using OR-Tools CP-SAT.

## Optimization Approach

A brute-force approach would require evaluating a large number of possible transporter combinations and lane assignments. This becomes computationally expensive as the number of lanes and transporters increases.

To address this, the application uses Google OR-Tools CP-SAT.

The optimization is performed in two stages:

### Stage 1: Maximize Transporter Utilization

The first optimization stage maximizes the number of distinct transporters selected, subject to the maximum transporter limit K.

### Stage 2: Minimize Transportation Cost

The second stage minimizes the total transportation cost while preserving the maximum number of transporters obtained in the first stage.

This ensures that the application follows a lexicographic optimization objective: transporter utilization has priority over cost minimization.

The solver uses integer cost values and only accepts an optimal solution.

## Database Design

PostgreSQL is used for persistent storage.

Flyway manages database schema versioning and migrations.

The database stores:
- Admin user information.
- Admin registration state.
- Transportation datasets.
- Lane and transporter information.
- Transporter lane quotes.
- Assignment history.

Dataset and assignment records are associated with their owning admin account.

## Authentication and Security

The application uses Spring Security and JWT-based authentication.

Key security features include:
- Admin registration using a bootstrap secret.
- Password hashing.
- JWT-based authentication.
- Protected API endpoints.
- Admin-specific data ownership and access control.

Production secrets are supplied through environment variables and are not stored in the source code.

## API Documentation

Interactive API documentation is available through Swagger UI:

https://transportation-cost-optimizer.onrender.com/swagger-ui/index.html

OpenAPI specification:

https://transportation-cost-optimizer.onrender.com/v3/api-docs

Health check:

https://transportation-cost-optimizer.onrender.com/actuator/health

## Sample Input

The dataset input API accepts transportation lanes and transporter quotes.

Example request:

```json
{
  "lanes": [
    {
      "id": 1,
      "origin": "Mumbai",
      "destination": "Delhi"
    }
  ],
  "transporters": [
    {
      "id": 101,
      "name": "Blue Dart Logistics",
      "laneQuotes": [
        {
          "laneId": 1,
          "quote": 5000
        }
      ]
    }
  ]
}
```

## Running Locally

### Prerequisites

- Java 21
- Maven
- PostgreSQL
- Docker (optional)

### 1. Clone the repository

```bash
git clone https://github.com/abhay-053/transportation-cost-optimizer.git

cd transportation-cost-optimizer
```

### 2. Configure environment variables

Configure the following variables before starting the application:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=transportation_cost_db
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

JWT_SECRET=your_secure_jwt_secret
JWT_EXPIRATION_MS=3600000

ADMIN_BOOTSTRAP_SECRET=your_admin_bootstrap_secret
SERVER_PORT=8080
```

Use secure values for secrets. Do not commit production credentials to GitHub.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The application will be available at:

http://localhost:8080

### 4. Run tests

```bash
./mvnw test
```

## Docker

Build the Docker image:

```bash
docker build -t transportation-cost-optimizer .
```

Run the container with the required environment variables:

```bash
docker run -p 8080:8080 \
  -e SERVER_PORT=8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=transportation_cost_db \
  -e DB_USERNAME=your_database_username \
  -e DB_PASSWORD=your_database_password \
  -e JWT_SECRET=your_secure_jwt_secret \
  -e JWT_EXPIRATION_MS=3600000 \
  -e ADMIN_BOOTSTRAP_SECRET=your_admin_bootstrap_secret \
  transportation-cost-optimizer
```

The database must be reachable from the container.

## Deployment

The application is deployed using Render with:
- A Docker-based Spring Boot web service.
- A managed PostgreSQL database.
- Environment-based configuration for credentials and application secrets.

Public API:

https://transportation-cost-optimizer.onrender.com

## Testing

The project includes automated tests for application components and optimization-related functionality.

Run the complete test suite using:

```bash
./mvnw test
```

## Future Improvements

- Add support for larger transportation datasets.
- Improve optimization performance for larger problem instances.
- Add more detailed optimization metrics.
- Introduce additional integration and API-level tests.
- Improve monitoring and observability.

## Author

Abhay Dhek

GitHub: https://github.com/abhay-053
