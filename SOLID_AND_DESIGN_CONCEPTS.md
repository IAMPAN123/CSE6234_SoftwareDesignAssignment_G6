# SOLID Principles and Software Design Concepts

## Overview
This document describes the SOLID software design principles and design concepts implemented in the POS (Point of Sale) System project.

---

## SOLID Principles

### 1. Single Responsibility Principle (SRP)

A class should have only one reason to change, meaning it should have only one responsibility. In this project, each class focuses on a specific concern: `Product` manages product data, `ProductDAO` handles database operations, `PaymentMethod` implementations handle specific payment types, and `CheckoutFacade` orchestrates the checkout process. This separation makes classes easier to understand, modify, and test.

```java
// Product.java - Only responsible for product data
public class Product {
    private int id, String barcode, String name, double price, int stock;
    public int getId() { return id; }
    public String getName() { return name; }
}

// ProductDAO.java - Only responsible for database operations
public class ProductDAO {
    public void addProduct(Product product) throws SQLException { ... }
    public List<Product> getAllProducts() throws SQLException { ... }
}
```

---

### 2. Open/Closed Principle (OCP)

Software entities should be open for extension but closed for modification. The payment system demonstrates this through the `PaymentMethod` interface, which allows new payment types (CardPayment, CashPayment, EWalletPayment) to be added without modifying existing code. When a new payment method is needed, developers simply create a new implementation of `PaymentMethod` and register it in `PaymentFactory`, rather than changing the existing payment classes.

```java
public interface PaymentMethod {
    boolean processPayment(double amount);
    String getPaymentType();
}

// New payment types can be added without modifying existing code
public class ApplePayPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) { return true; }
    @Override
    public String getPaymentType() { return "ApplePay"; }
}
```

---

### 3. Liskov Substitution Principle (LSP)

Objects of a superclass should be replaceable with objects of its subclasses without breaking the application. All `PaymentMethod` implementations (CardPayment, CashPayment, EWalletPayment) can be used interchangeably in the `CheckoutFacade.checkout()` method. The system doesn't need to know which concrete payment type is being used—it works consistently with any implementation.

```java
public Receipt checkout(String paymentType) throws SQLException {
    PaymentMethod payment = PaymentFactory.createPayment(paymentType);
    // Works with ANY PaymentMethod implementation without modification
    if (!payment.processPayment(total)) {
        throw new RuntimeException("Payment failed");
    }
}
```

---

### 4. Interface Segregation Principle (ISP)

Clients should not be forced to depend on interfaces they do not use. The `PaymentMethod` interface is minimal and focused, containing only the essential methods (`processPayment()` and `getPaymentType()`) that all payment implementations need. This prevents classes from being burdened with unnecessary methods they don't use.

```java
// Focused, small interface
public interface PaymentMethod {
    boolean processPayment(double amount);
    String getPaymentType();
}
// Classes only implement what they need, not bloated interfaces
```

---

### 5. Dependency Inversion Principle (DIP)

High-level modules should not depend on low-level modules; both should depend on abstractions. `CheckoutFacade` depends on abstract DAOs (ProductDAO, TransactionDAO) and the `PaymentMethod` interface, not concrete implementations. This loose coupling allows changes to database operations or payment processing without affecting the high-level checkout logic.

```java
public class CheckoutFacade {
    private ProductDAO productDAO;  // Depends on abstraction
    private TransactionDAO transactionDAO;  // Depends on abstraction
    
    public Receipt checkout(String paymentType) throws SQLException {
        PaymentMethod payment = PaymentFactory.createPayment(paymentType);  // Uses interface
    }
}
```

---

## Software Design Concepts

### 1. Abstraction

Abstraction is hiding complex implementation details and showing only essential features. The project uses abstraction through interfaces (PaymentMethod) and DAOs to hide database and payment processing complexity. Clients interact with simple method calls like `payment.processPayment(amount)` and `productDAO.addProduct(product)` without needing to understand SQL queries or payment gateway details.

```java
public interface PaymentMethod {
    boolean processPayment(double amount);
    String getPaymentType();
}

// Client code is simple - no need to know payment implementation details
PaymentMethod payment = PaymentFactory.createPayment("CARD");
boolean success = payment.processPayment(100.0);
```

---

### 2. Encapsulation

Encapsulation bundles data (variables) and methods into a single class while hiding internal details using access modifiers. The `Product` class encapsulates product data with private fields and provides controlled access through public getters and setters. Similarly, `ProductDAO` encapsulates the database connection and hides SQL complexity, allowing clients to use simple methods like `addProduct()` and `getAllProducts()`.

```java
public class Product {
    private int id, String barcode, String name, double price, int stock;
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    // Controlled access to private data
}
```

---

### 3. Modularity

Modularity breaks down a system into smaller, independent, loosely coupled modules that can be developed and maintained separately. The project is organized into distinct modules: database layer (ProductDAO, TransactionDAO), model layer (Product, ShoppingCart), payment layer (PaymentMethod, CardPayment), service layer (CheckoutFacade), and UI layer (LoginPanel, AdminPanel). Each module can be understood, tested, and modified independently.

```
src/main/java/com/pos/
├── db/           (Database Module)
├── model/        (Domain Model Module)
├── payment/      (Payment Module)
├── service/      (Service Module)
└── ui/           (UI Module)
```

---

### 4. Coupling and Cohesion

Cohesion measures how closely related the responsibilities within a class are, while coupling measures dependencies between classes. The project achieves high cohesion—`Product` contains only product-related data, `ProductDAO` contains only database operations—and low coupling by depending on abstractions rather than concrete classes. `CheckoutFacade` depends on `ProductDAO` and `PaymentMethod` interfaces, not their implementations, making the system flexible and maintainable.

```java
// High Cohesion: All members in Product relate to product data
public class Product {
    private int id, String barcode, String name, double price, int stock;
}

// Low Coupling: Depends on interfaces, not concrete classes
public class CheckoutFacade {
    private ProductDAO productDAO;  // Interface/abstraction
    private PaymentMethod payment;  // Interface/abstraction
}
```

---

### 5. Polymorphism

Polymorphism allows objects to take on multiple forms and enables writing generic code that works with different types. The payment system uses polymorphism—`CardPayment`, `CashPayment`, and `EWalletPayment` all implement `PaymentMethod`, allowing the same `checkout()` method to handle any payment type. Adding new payment types only requires creating a new implementation without changing existing checkout logic.

```java
// Same checkout method works with ANY PaymentMethod implementation
public Receipt checkout(String paymentType) throws SQLException {
    PaymentMethod payment = PaymentFactory.createPayment(paymentType);
    if (!payment.processPayment(total)) {
        throw new RuntimeException("Payment failed");
    }
}
// Works with Card, Cash, EWallet, or any future payment type
```

---

### 6. Composition Over Inheritance

Composition involves including instances of other classes as members rather than inheriting from a parent class. `ShoppingCart` contains `CartItem` objects, `CheckoutFacade` contains `ShoppingCart`, `ProductDAO`, and `TransactionDAO` instances, and `Receipt` contains a list of `CartItem` objects. This approach provides more flexibility—composition allows changing object relationships at runtime and avoids rigid inheritance hierarchies.

```java
// Composition: ShoppingCart contains CartItem instances
public class ShoppingCart {
    private List<CartItem> items;  // Composition
    public void addItem(Product product, int quantity) {
        items.add(new CartItem(product, quantity));
    }
}
```

---

## Summary

This POS System project demonstrates strong software design practices through adherence to SOLID principles and fundamental design concepts. The application maintains clear separation of concerns with high cohesion within modules and low coupling between them. By depending on abstractions like interfaces and DAOs rather than concrete implementations, the codebase remains flexible and extensible. The strategic use of composition, polymorphism, and encapsulation enables the system to be easily maintained, tested, and extended with new features without modifying existing code.

| Design Principle | Implementation |
|------------------|-----------------|
| **SRP** | Each class has single responsibility (Product, DAOs, Payment types) |
| **OCP** | New features added without modifying existing code |
| **LSP** | All implementations are interchangeable |
| **ISP** | Focused, minimal interfaces |
| **DIP** | Depends on abstractions, not concrete classes |
| **Abstraction** | Interfaces and DAOs hide complexity |
| **Encapsulation** | Private fields with controlled access |
| **Modularity** | Organized into independent modules |
| **Cohesion** | Related responsibilities grouped together |
| **Coupling** | Dependencies through abstractions |
| **Polymorphism** | Multiple types work through same interface |
| **Composition** | Objects contain instances of other classes |
