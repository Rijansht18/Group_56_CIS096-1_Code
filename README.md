# E-Commerce Application

This is a Java-based e-commerce application designed to manage an online store. It allows customers to browse products, add them to a cart, and place orders. Admins can manage products, view orders, and update their status. The application is built using **JavaFX** for the user interface and **MySQL** for database management.

---

## Features

### For Customers:
- **Browse Products**: View a list of available products with details like name, price, and stock.
- **Add to Cart**: Add products to the cart with a specified quantity.
- **Place Orders**: Place orders using items in the cart and provide a shipping address.
- **View Order History**: Check the status of previous orders.

### For Admins:
- **Manage Products**: Add, update, or delete products.
- **Manage Orders**: View all orders and update their status (e.g., PENDING, SHIPPED, DELIVERED).
- **Manage Users**: View and manage customer and admin accounts.

<<<<<<< HEAD
---

## Technologies Used

- **Java**: Core programming language.
- **JavaFX**: For building the user interface.
- **MySQL**: For database management.
- **JDBC**: For connecting the application to the MySQL database.
- **Maven**: For dependency management and building the project.

---

## Architecture

The application follows a **layered architecture**:

1. **Presentation Layer**: Handles the user interface (JavaFX).
2. **Business Layer**: Contains the core logic (e.g., `ProductService`, `OrderService`).
3. **Data Access Layer**: Manages database interactions (e.g., `ProductRepository`, `OrderRepository`).
4. **Model Layer**: Represents data structures (e.g., `Product`, `Customer`, `Order`).

---

## Design Patterns Used

1. **Singleton**: Ensures a single instance of critical components like `DatabaseManager`.
2. **Factory**: Simplifies object creation (e.g., `User` objects).
3. **Observer**: Notifies users of order status changes.
4. **MVC (Model-View-Controller)**: Separates UI, logic, and data for better organization.

---

## Setup Instructions

### Prerequisites
- **Java Development Kit (JDK)**: Version 11 or higher.
- **MySQL**: Installed and running locally or remotely.
- **Maven**: For dependency management.

### Steps to Run the Project

1. **Clone the Repository**:
   ```bash
   https://github.com/Rijansht18/Group_56_CIS096-1_Code.git
   cd ecommerceSystem
2. Build the project with Maven:
   ```bash
   mvn clean package
3. Run the application:
   ```bash
   mvn javafx:run
=======
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
>>>>>>> d5d86a3 (Update README.md)
