# 🚀 TaskFlow — Project & Task Management System

**A secure, RESTful backend application built with Java and Spring Boot to simplify project organization, task assignment, and team collaboration.**

---

## 📌 Overview

TaskFlow is a backend-focused project management and task tracking application developed using **Java 21 and Spring Boot**.

It provides a structured way to manage projects, organize tasks, assign responsibilities, and monitor work progress. The application exposes RESTful APIs and uses role-based access control to manage user permissions.

The project is designed using a layered architecture, following software engineering practices such as separation of concerns, DTO-based communication, input validation, centralized exception handling, and automated testing.

## ✨ Key Features

### 🔐 Authentication & Security

- JWT-based authentication.
- Secure login functionality.
- Role-based authorization using Spring Security.
- Protected API endpoints.

### 📁 Project Management

- Create and manage projects.
- Retrieve project details.
- Update existing project information.
- Delete projects.
- Manage project members.

### ✅ Task Management

- Create, retrieve, update, and delete tasks.
- Assign tasks to users.
- Retrieve tasks by project or user.
- Filter tasks by status and priority.
- Search tasks by title.
- Pagination for task retrieval.

### 💬 Collaboration

- Comment management.
- Notification management.
- Project member management.

### ⚙️ Backend Engineering

- Layered application architecture.
- DTO-based request and response handling.
- Bean validation.
- Centralized exception handling.
- RESTful API design.
- Database integration using Spring Data JPA.

## 🛠️ Technology Stack

| Technology        | Usage                           |
| ----------------- | ------------------------------- |
| Java 21           | Core programming language       |
| Spring Boot 4.1.2 | Backend application framework   |
| Spring Security   | Application security            |
| JWT               | Token-based authentication      |
| Spring Data JPA   | Data access and persistence     |
| Hibernate         | Object-relational mapping       |
| MySQL             | Relational database             |
| Maven             | Dependency and build management |
| JUnit 5           | Automated testing               |
| Mockito           | Mocking and unit testing        |
| MockMvc           | Controller/API testing          |
| Git & GitHub      | Version control                 |

## 🏗️ Application Architecture

TaskFlow follows a **layered architecture** to maintain modularity, readability, and separation of responsibilities.

```text
                 CLIENT
                    |
                    v
            REST CONTROLLERS
                    |
                    v
             SERVICE LAYER
              (Business Logic)
                    |
                    v
            REPOSITORY LAYER
                    |
                    v
              MYSQL DATABASE
```

### Architectural Components

| Layer      | Responsibility                                        |
| ---------- | ----------------------------------------------------- |
| Controller | Handles HTTP requests and responses                   |
| Service    | Implements business logic                             |
| Repository | Performs database operations                          |
| Entity     | Represents database entities                          |
| DTO        | Transfers data between application layers and clients |
| Security   | Handles authentication and authorization              |
| Exception  | Provides centralized error handling                   |

## 🔒 Security Implementation

Security is implemented using Spring Security and JSON Web Tokens (JWT).

- Authenticated access to protected endpoints.
- Role-based access restrictions.
- Authorization at the controller level.
- JWT-based authentication flow.

The application supports role-based access for **ADMIN, PROJECT_MANAGER, and MEMBER** users.

## 📡 API Modules

The backend is organized into the following API modules:

| Module         | Description                                                    |
| -------------- | -------------------------------------------------------------- |
| Authentication | User login and JWT authentication                              |
| Users          | User-related operations                                        |
| Projects       | Project creation and management                                |
| Tasks          | Task assignment, retrieval, updating, filtering, and searching |
| Comments       | Comment-related operations                                     |
| Notifications  | Notification-related operations                                |

## 🧪 Testing & Quality Assurance

Testing is an important part of TaskFlow development.

The application has been tested using JUnit 5, Mockito, Spring Boot Test, and MockMvc.

### Final Test Execution

| Metric               |      Result |
| -------------------- | ----------: |
| Total Tests Executed |     **389** |
| Passed               |     **389** |
| Failures             |       **0** |
| Errors               |       **0** |
| Skipped              |       **0** |
| Build Status         | **SUCCESS** |

### Testing Coverage

- Repository testing
- Service integration testing
- Controller testing
- API validation testing
- Exception handling testing
- Authorization testing

\<details>
\<summary>\<strong>View testing evidence\</strong>\</summary>

Testing execution screenshots are available in the [`screenshots/`](screenshots/) directory.

\</details>

## 📂 Project Structure

```text
TaskFlow/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/taskflow/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│           └── com/taskflow/
│
├── screenshots/
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

*Package and folder names should be adjusted to match the actual repository structure.*

## ⚡ Getting Started

Follow these steps to run TaskFlow locally.

### Prerequisites

Make sure you have installed:

- Java 21 or later.
- MySQL.
- Git.
- An IDE such as IntelliJ IDEA, Eclipse, or VS Code.

### 1. Clone the repository

```bash
git clone https://github.com/anushkaa45/TaskFlow---Task-Management-Project.git
```

### 2. Navigate to the project directory

```bash
cd TaskFlow---Task-Management-Project
```

### 3. Configure the database

Create a MySQL database and configure your database connection in `application.properties`.

Add your database URL, username, and password locally. Keep sensitive credentials out of version control.

### 4. Run the application

For Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### 5. Run automated tests

```powershell
.\mvnw.cmd test
```

## 📸 Project Screenshots

Add application screenshots and testing evidence here.

| Screenshot     | Description                            |
| -------------- | -------------------------------------- |
| Application    | Main application interface             |
| API Testing    | REST API request and response examples |
| Test Execution | Maven test execution results           |
| Test Structure | Automated test package structure       |

See the [`screenshots/`](screenshots/) directory for testing evidence.

## 🚧 Future Enhancements

Potential improvements for future versions:

- Frontend integration.
- Advanced project analytics and reporting.
- Real-time notifications.
- Improved API documentation.
- Deployment and production monitoring.

## 🎯 Learning Outcomes

Through this project, I have gained practical experience in:

- Developing RESTful APIs using Spring Boot.
- Implementing authentication and authorization.
- Designing layered backend applications.
- Integrating MySQL using Spring Data JPA.
- Handling validation and application exceptions.
- Writing automated unit and integration tests.
- Using Git and GitHub for version control.

## 👩‍💻 Author

**Anushka Khade**

Artificial Intelligence and Data Science Engineering Student

Interested in Backend Development, Java, Spring Boot, and Software Engineering.

## 🔗 Repository

**GitHub:** [TaskFlow – Task Management Project](https://github.com/anushkaa45/TaskFlow---Task-Management-Project)

---

⭐ If you find this project interesting, feel free to explore the repository!
