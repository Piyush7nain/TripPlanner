Trip Planner Application
This is a Spring Boot RESTful API application designed to manage travel trips and their associated destinations.
It provides a robust backend for planning and organizing trips, including comprehensive CRUD (Create, Read, Update, Delete)
operations, and flexible data import/export capabilities.

Key Features:
    Trip Management: Full CRUD operations for Trip entities.
    Destination Management: Destination entities linked to Trips via a One-to-Many relationship, allowing for detailed planning of each stop within a trip.
    Data Validation: Robust input validation using Jakarta Bean Validation (@Valid, @NotBlank, @NotNull, etc.) to ensure data integrity.
    Global Error Handling: Centralized exception handling to provide consistent and informative error responses (e.g., 400 Bad Request for validation errors, 404 Not Found for missing resources).
    CSV Import/Export:
    Export all trips to a CSV file.
    Import new trips from an uploaded CSV file.
    JSON Import (File Upload): Import trips, including their nested destinations, from an uploaded JSON file.
    JSON Batch Import (Direct POST): Import multiple trips with nested destinations by sending a JSON array directly in the request body.
    Layered Architecture: Follows a standard Controller-Service-Repository architecture for clear separation of concerns, maintainability, and testability.
    In-Memory Database: Uses H2 Database for easy setup and development, with Hibernate (JPA) for ORM.
    Lombok: Utilizes Lombok to reduce boilerplate code (getters, setters, constructors).
    Testing: Comprehensive unit tests for all layers using JUnit 5 and Mockito.

How to Get the Application Running
    This section guides you through setting up and running the Trip Planner application on your local machine.

    Prerequisites
        Before you begin, ensure you have the following installed:
        Java Development Kit (JDK): Version 17 or higher.
        Apache Maven: Version 3.6.0 or higher.

    Steps
        Clone the Repository (Hypothetical): If this were a Git repository, you would clone it:
        git clone https://github.com/Piyush7nain/TripPlanner.git
        cd trip-planner

    For this context, ensure you have all the .java files in their correct package structure and the pom.xml in the root directory.
    Build the Project: Open your terminal or command prompt, navigate to the root directory of the trip-planner project (where pom.xml is located), and run the Maven build command:

    mvn clean install
    This command compiles the source code, runs tests, and packages the application.

    Configure H2 Database: The application uses an in-memory H2 database for simplicity during development. The configuration is in src/main/resources/application.properties.

    # H2 Database Configuration
    spring.h2.console.enabled=true
    spring.h2.console.path=/h2-console
    spring.datasource.url=jdbc:h2:mem:tripplannerdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=

    # JPA/Hibernate Configuration
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    No changes are typically needed here unless you want to customize port or database name.
    Run the Application: From the root directory of your project, execute the Spring Boot run command:

    mvn spring-boot:run
    You should see logs indicating that the application is starting up. Once it's ready, you'll see a message like: Started TripPlannerApplication in X.XXX seconds (JVM running for Y.YYY).

    Access H2 Console (Optional, for database inspection): While the application is running, open your web browser and navigate to: http://localhost:8080/h2-console

    JDBC URL: Ensure it matches jdbc:h2:mem:tripplannerdb
    User Name: sa
    Password: (leave blank) Click "Connect". You can then browse the TRIPS and DESTINATIONS tables created by Hibernate.
    How to Test Using Postman
    Once the application is running, you can use Postman (or any other API client like Insomnia, curl, etc.) to test the RESTful endpoints. The base URL for all endpoints is http://localhost:8080.

General Postman Tips:
    Headers: For POST and PUT requests sending JSON, set the Content-Type header to application/json.
    Body: For POST and PUT requests with JSON data, select raw and JSON in the body tab. For file uploads, select form-data in the body tab, and set the value type to File.

API Endpoints
1. Health Check
    Endpoint: GET /trips/health
    Description: Verifies that the application is up and running.
    Expected Response: 200 OK with plain text OK.

2. Create a New Trip
    Endpoint: POST /trips
    Description: Adds a new travel trip.
    Body (JSON - raw):
    {
        "name": "Japan Cherry Blossom Tour",
        "startDate": "2025-04-01",
        "endDate": "2025-04-10",
        "description": "A scenic trip to witness cherry blossoms across Japan."
    }
    Expected Response: 201 Created with the created trip object and a Location header pointing to the new resource.

3. Get All Trips
    Endpoint: GET /trips
    Description: Retrieves a list of all trips currently in the system.
    Expected Response: 200 OK with a JSON array of trips.

4. Get Trip by ID
    Endpoint: GET /trips/{id} (e.g., /trips/1 - use an ID from a trip you created)
    Description: Retrieves a single trip by its unique identifier.
    Expected Response: 200 OK with the trip object, or 404 Not Found if the ID does not exist.

5. Add Destination to a Trip
    Endpoint: POST /trips/{tripId}/destinations (e.g., /trips/1/destinations)
    Description: Adds a new destination to an existing trip.
    Body (JSON - raw):
    {
        "name": "Mount Fuji",
        "location": "Honshu, Japan",
        "arrivalDate": "2025-04-05",
        "departureDate": "2025-04-06"
    }
    Expected Response: 201 Created with the created destination object and a Location header.

6. Get All Destinations for a Trip
    Endpoint: GET /trips/{tripId}/destinations (e.g., /trips/1/destinations)
    Description: Retrieves all destinations associated with a specific trip.
    Expected Response: 200 OK with a JSON array of destinations (can be empty if no destinations exist for the trip).

7. Export All Trips to CSV
    Endpoint: GET /trips/export
    Description: Downloads all trip data as a CSV file.
    Expected Response: 200 OK, with Content-Type: text/csv and Content-Disposition: attachment; filename="trips.csv" headers. Postman will show raw CSV data.

8. Import Trips from CSV File
    Endpoint: POST /trips/import
    Description: Uploads a CSV file containing trip data to create new trips.
    Body (form-data):
    Set Key to file.
    Change Value type from Text to File.
    Click Select File and choose your CSV file.
    Example trips_to_import.csv content:
    name,startDate,endDate,description
    Beach Getaway,2026-06-01,2026-06-07,Relaxing by the sea
    City Exploration,2026-07-15,2026-07-20,Visiting famous landmarks
    Expected Response: 200 OK with a message like Successfully imported X trips.

9. Import Trips with Nested Destinations from JSON File
    Endpoint: POST /trips/import-json
    Description: Uploads a JSON file containing trips with nested destination data.
    Body (form-data):
    Set Key to file.
    Change Value type from Text to File.
    Click Select File and choose your JSON file.
    Example trips_with_destinations.json content:
    [
      {
        "name": "European Grand Tour",
        "startDate": "2025-07-01",
        "endDate": "2025-07-30",
        "description": "A month-long journey across Europe's iconic cities.",
        "destinations": [
          {
            "name": "Eiffel Tower",
            "location": "Paris, France",
            "arrivalDate": "2025-07-03",
            "departureDate": "2025-07-07"
          },
          {
            "name": "Colosseum",
            "location": "Rome, Italy",
            "arrivalDate": "2025-07-10",
            "departureDate": "2025-07-14"
          }
        ]
      }
    ]
    Expected Response: 200 OK with a message like Successfully imported X trips with their destinations.

10. Batch Import Trips with Nested Destinations (Direct JSON)
    Endpoint: POST /trips/batch
    Description: Imports multiple trips with nested destinations by sending a JSON array directly in the request body (no file upload needed).
    Body (JSON - raw):
    [
      {
        "name": "Batch Trip London",
        "startDate": "2025-11-01",
        "endDate": "2025-11-05",
        "description": "A quick visit to London.",
        "destinations": [
          {
            "name": "Big Ben",
            "location": "London, UK",
            "arrivalDate": "2025-11-02",
            "departureDate": "2025-11-02"
          }
        ]
      },
      {
        "name": "Batch Trip Berlin",
        "startDate": "2025-12-01",
        "endDate": "2025-12-03",
        "description": "Exploring historical sites in Berlin.",
        "destinations": []
      }
    ]
    Expected Response: 200 OK with a message like Successfully imported X trips with their destinations.