# Twitter-like Board Application

A Twitter-like board application built with Spring Boot 3 and React, featuring Google OAuth SSO authentication and asynchronous post operations.

## Tech Stack

### Backend
- Java 17
- Spring Boot 3
- Spring Security with OAuth2
- Spring Data JPA
- MySQL
- Redis
- JWT Authentication
- Gradle

### Frontend
- React 18
- React Router
- Axios
- CSS3

## Features

- Google OAuth SSO Authentication
- Create, Read, Update, Delete posts (with async operations)
- User profile management
- Post browsing and filtering
- RESTful API design
- Responsive web interface

## Getting Started

### Prerequisites

- Java 17+
- Node.js 16+
- Docker & Docker Compose
- MySQL 8.0+
- Redis

### Environment Setup

1. Copy environment files:
```bash
cp .env.example .env
cp frontend/.env.example frontend/.env
```

2. Configure Google OAuth:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing one
   - Enable Google+ API
   - Create OAuth 2.0 credentials
   - Update `.env` and `frontend/.env` with your client ID and secret

### Running with Docker

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Running Locally

#### Backend
```bash
cd backend
./gradlew bootRun
```

#### Frontend
```bash
cd frontend
npm install
npm start
```

### Database Setup

The application will automatically create the database schema on startup when running in development mode.

## API Documentation

The API follows RESTful principles and includes the following endpoints:

- `POST /api/v1/auth/google` - Google OAuth authentication
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `GET /api/v1/posts` - Get all posts (paginated)
- `POST /api/v1/posts` - Create new post (async)
- `GET /api/v1/posts/{id}` - Get specific post
- `PUT /api/v1/posts/{id}` - Update post (async)
- `DELETE /api/v1/posts/{id}` - Delete post (async)
- `GET /api/v1/users/{id}/posts` - Get user's posts
- `GET /api/v1/users/me` - Get current user info

## Testing

### Backend Tests
```bash
cd backend
./gradlew test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### E2E Tests
```bash
cd frontend
npm run cypress:open
```

## Project Structure

```
├── backend/                 # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/twitterboard/
│   │       ├── controller/  # REST controllers
│   │       ├── service/     # Business logic
│   │       ├── repository/  # Data access layer
│   │       ├── entity/      # JPA entities
│   │       ├── dto/         # Data transfer objects
│   │       ├── config/      # Configuration classes
│   │       ├── security/    # Security configuration
│   │       └── exception/   # Exception handling
│   └── src/main/resources/
│       └── application.yml  # Application configuration
├── frontend/                # React frontend
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── services/        # API services
│   │   ├── hooks/           # Custom hooks
│   │   ├── contexts/        # React contexts
│   │   └── utils/           # Utility functions
│   └── public/
└── docs/                    # Project documentation
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License.