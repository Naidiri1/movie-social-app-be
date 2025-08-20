# 🚀 Iriscope Backend - Movie Social Platform 

**[🔴 Live Production API](https://your-railway-url.up.railway.app)** | **[⚛️ Frontend Repository](https://github.com/Naidiri1/movie-social-app-fe)** | **[🎬 Live Demo](https://movie-social-app-fe-57kb.vercel.app/login)**

## 🎯 Why This Project Matters

I built Iriscope's backend as a RESTful API that powers a social movie platform where users can discover, track, and share their movie preferences. The backend successfully integrates with TMDB's movie database and handles user authentication, movie collections, and social features.



## 🏗️ Architecture Overview
<img width="756" height="778" alt="Screenshot 2025-08-19 194546" src="https://github.com/user-attachments/assets/2500c62e-f5f1-456c-a867-39d38bdb6718" />

*Complex PostgreSQL schema handling movies, users, reviews, rankings, and social interactions*

## 💡 Core Achievements

### Key Accomplishments
- **Fully functional REST API** with 40+ endpoints
- **Secure user authentication** using JWT tokens
- **Database design** with 10+ related tables
- **TMDB API integration** for real-time movie data
- **Successfully deployed** to production on Railway

### Technical Implementation
- Built with **Spring Boot** and **Java**
- **PostgreSQL database** for data persistence
- **JWT authentication** for secure access
- **MVC architecture** with clear separation of concerns
- **RESTful design** following HTTP conventions
### Tech Stack
- **Spring Boot** - Java framework for building the API
- **PostgreSQL** - Relational database
- **JWT** - Token-based authentication
- **Railway** - Cloud deployment platform
- **TMDB API** - External movie data source

## 🌟 Core Features Implemented

### 🎬 Movie Management
- Fetch popular and upcoming movies from TMDB
- Browse movies by genre
- Search functionality across movie database
- Pagination for large result sets
- Cache frequently accessed movie data

### 👤 User System
- User registration and login
- Profile management
- Secure password storage with BCrypt
- JWT tokens for maintaining sessions
- User preferences and settings

### 📚 Movie Collections
Users can organize movies into:
- **Favorites** - Movies they love
- **Watched** - Movies they've seen
- **Watch Later** - Movies to watch in the future
- **Top 10** - Personal ranking system

### ⭐ Reviews & Ratings
- Create, read, update, and delete reviews
-  Rating system (1-10)
- View reviews by movie or user
- Like and dislike reviews

### 🔄 Drag-and-Drop Top 10
- Custom ranking system for top 10 movies
- Reorder movies with position tracking
- Maintain order consistency in database
- Add comments to each ranked movie

## 📸 Project Structure

<img width="420" height="641" alt="Screenshot 2025-08-20 012340" src="https://github.com/user-attachments/assets/2b9e3f6f-114d-43c9-908c-75f9565b4654" />


*Spring Boot project structure with organized packages for controllers, services, repositories, and models*

## 🚀 Production Deployment

<img width="1822" height="711" alt="Screenshot 2025-08-20 012503" src="https://github.com/user-attachments/assets/461409ec-c935-4ac6-a4ae-edbb54c71385" />

*Live deployment on Railway with PostgreSQL database*

### Deployment Features
- Deployed successfully to Railway cloud
- Connected PostgreSQL database
- Environment variables for sensitive data
- Automatic deploys from GitHub

## 🛡️ Security Features

### Authentication
- **Password encryption** using BCrypt
- **JWT tokens** for API access
- **Protected routes** requiring authentication

### Best Practices
- CORS configuration for frontend access
- Environment variables for sensitive data

## 📊 Database Design

### Database Structure
- **10+ tables** with proper relationships
- Foreign key constraints for data integrity
- Indexes on frequently queried columns
- Normalized design to reduce redundancy

### Main Tables
- `users` - User accounts and authentication
- `movies` - Movie information
- `user_movies` - Links users to their movie collections
- `reviews` - User reviews and ratings
- `top_ten_movies` - Ranked movie lists
- `likes` - Review likes and dislikes

## 🎓 Skills Demonstrated

### Backend Development
- **Java & Spring Boot** - Built complete REST API
- **Database design** - Created relational schema
- **API integration** - Connected with external TMDB API
- **Authentication** - Implemented JWT security
- **CRUD operations** - Full functionality for all features

### Problem Solving
- Figured out drag-and-drop ordering logic
- Implemented secure sharing system Private/Public APIs
- Handled pagination for large datasets
- Managed user sessions with tokens
- Solved Version Incopatibility in vercel with Nex.js, and Material Tailwind

### Development Practices
- Organized code structure
- RESTful API conventions
- Error handling and validation
- Environment configuration
- Version control with Git

## 📈 Learning Journey

Through this project, I:
- **Learned** Spring Boot framework from documentation
- **Implemented** JWT authentication for the first time
- **Designed** a complex relational database
- **Deployed** to production cloud environment
- **Integrated** with external APIs
- **Solved** real-world backend challenges

## 💻 Running the Project Locally

### Prerequisites
- Java 17
- Maven
- PostgreSQL
- TMDB API key

### Quick Start
1. Clone the repository
2. Set up PostgreSQL database
3. Add environment variables
4. Run with Maven
5. API available at `localhost:8080`

## 🤝 What This Project Shows

This backend demonstrates my ability to:
- **Build functional APIs** from scratch
- **Work with databases** and design schemas
- **Implement security** features
- **Deploy to production** environments
- **Integrate external services**
- **Solve technical challenges** independently

## 📬 Let's Connect!

**[Email](mailto:your.email@example.com)** | **[LinkedIn](https://linkedin.com/in/yourprofile)** | **[GitHub](https://github.com/Naidiri1)**

---

### 🔗 See the Complete Project

- **[Frontend Code](https://github.com/Naidiri1/movie-social-app-fe)** - React frontend
- **[Live Demo](https://movie-social-app-fe-57kb.vercel.app/login)** - Try it out!
- **[Video Walkthrough](https://youtu.be/duTqdjZNbug)** - 3-minute demo

---

*Built with passion and a great way to keep pushing my self to new challenges and leaning on the process enjoying it too*
