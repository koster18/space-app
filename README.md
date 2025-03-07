Project Description
This application allows users to obtain geographic coordinates from an address and visualize the night sky map above the observer's location. Additionally, the application makes requests to the CelesTrak website to retrieve satellite and space station orbital data. This data is stored in a PostgreSQL database, enabling users to see visible satellites and space stations in real-time or at a specified time.

Features
Geocoding
Users can enter an address, and the application returns its geographic coordinates (latitude and longitude).
Night Sky Visualization
The application displays a map of the night sky above the specified location, including stars, constellations, and other astronomical objects.
Satellite Data Retrieval
The application queries CelesTrak for current satellite and space station orbital data.
Data Storage
Retrieved data is stored in a PostgreSQL database for quick access and processing.
Visible Satellite Rendering
The application calculates the position of satellites and space stations using the Kepler-Newton model or the more accurate NASA SGP4 model.
Observation Time Selection
Users can specify a time for which they wish to see visible satellites and space stations.

Technologies
Programming Language: Java
Database: PostgreSQL
Libraries and Frameworks:
I used SGP4 library https://github.com/neuromorphicsystems/sgp4
I used Greg Miller`s Celestial programming page for some formulas, stars db and constellations https://celestialprogramming.com
I used CelesTrak for some formulas and API for satellite data retrieval https://celestrak.org/
I used openstreetmap Api for resolving geo position of user https://nominatim.openstreetmap.org

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/koster18/space-app/
   cd space-app

2. **Install Dependencies**:

   docker-compose up

3. **Run the Application**:

   http://localhost:8081/skymap

## License
This project is licensed under the Unlicense. 