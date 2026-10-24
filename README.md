# CampusBite — Canteen Pre-Order and Token Management System

A beginner-friendly Java web project using Spring Boot, REST APIs, and HTML/CSS/JavaScript.

## Features
- View canteen food items, prices, and available stock
- Add menu items and edit price/stock
- Place an order with multiple items
- Validate stock before accepting an order
- Calculate the total bill and generate a unique token (example: `CT-1001`)
- Automatically reduce stock after accepted orders
- Track/update order status: Pending, Preparing, Completed

## Requirements
- Java 17 or newer
- Maven 3.6+ (or open the project in IntelliJ IDEA / Eclipse with Maven support)
- Internet access the first time Maven downloads dependencies

## Run
1. Extract the ZIP file.
2. Open a terminal in the `CanteenPreOrderSystem` folder.
3. Run: `mvn spring-boot:run`
4. Open `http://localhost:8080` in your browser.

If you use an IDE, import the folder as an existing Maven project and run `CanteenApplication.java`.

## Notes
- The app uses in-memory demo data. Food items and orders reset when the Java server restarts.
- The menu management area is a classroom demo, not protected by login. Add authentication and a database before real-world deployment.
- Currency is shown in Indian rupees (₹).
