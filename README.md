# E-Commerce Application with JavaFX

This is a simple e-commerce application built with JavaFX. The application has two types of users: customers and administrators. Customers can browse products, add them to a cart, and place orders. Administrators can manage products, orders, and customers.

## Features

### Customer Features
- Browse products
- Search and filter products by category
- Add products to cart
- View and manage shopping cart
- Place orders
- View order history
- Update profile information

### Admin Features
- Manage products (add, update, delete)
- Manage orders (view, update status)
- Manage customers (view, update, delete)
- Update profile information

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven

## How to Run

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/ecommerce-javafx.git
   cd ecommerce-javafx
   ```

2. Build the project with Maven:
   ```
   mvn clean package
   ```

3. Run the application:
   ```
   mvn javafx:run
   ```

## Default Users

The application comes with two default users for testing:

### Admin
- Username: admin
- Password: admin123

### Customer
- Username: customer
- Password: customer123

## Project Structure

- `src/main/java/com/ecommerce/model`: Contains the model classes (User, Customer, Admin, Product, Cart, Order, etc.)
- `src/main/java/com/ecommerce/service`: Contains the service classes for business logic
- `src/main/java/com/ecommerce/ui`: Contains the UI classes for the JavaFX application
- `src/main/resources/styles`: Contains CSS files for styling the UI

## Technologies Used

- Java 17
- JavaFX 17
- Maven

## License

This project is licensed under the MIT License - see the LICENSE file for details. 