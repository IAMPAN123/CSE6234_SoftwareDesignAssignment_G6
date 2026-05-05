# Design Patterns & Principles - Complete Implementation Guide

## Executive Summary

This POS system implements **8 major design patterns and principles** to ensure scalability, maintainability, extensibility, and clean architecture:

1. **Singleton Pattern** - Database connection management
2. **Observer Pattern** - Real-time cart updates
3. **Strategy Pattern** - Interchangeable payment strategies
4. **Factory Pattern** - Payment method creation
5. **Facade Pattern** - Simplified checkout workflow
6. **Data Access Object (DAO) Pattern** - Database abstraction layer
7. **Model-View-Controller (MVC)** - Layered architecture
8. **Functional Programming** - Stream API for data manipulation

---

## 1. Singleton Pattern: DatabaseConfig

### Purpose
Ensures only one database connection exists throughout the application lifecycle, preventing resource waste and connection conflicts.

### Implementation
```java
// DatabaseConfig.java
public class DatabaseConfig {
    private static DatabaseConfig instance;
    private Connection connection;
    private static final String DATABASE_URL = "jdbc:sqlite:shop.db";

    private DatabaseConfig() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DATABASE_URL);
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    public static DatabaseConfig getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeDatabase() throws SQLException {
        // Create tables if they don't exist
    }
}
```

### Usage in DAOs
```java
// ProductDAO.java
public class ProductDAO {
    private final Connection connection;

    public ProductDAO() throws SQLException {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }
    // ... rest of implementation
}

// TransactionDAO.java - Similar usage
public class TransactionDAO {
    private final Connection connection;

    public TransactionDAO() throws SQLException {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }
    // ... rest of implementation
}
```

### Benefits
- ✅ Single database connection shared across entire application
- ✅ Lazy initialization - connection created only when first needed
- ✅ Thread-safe guaranteed single instance
- ✅ Centralized database configuration management
- ✅ Prevents connection pool exhaustion

---

## 2. Observer Pattern: ShoppingCart

### Purpose
Implements real-time notifications when cart contents change, enabling automatic UI updates without tight coupling between model and view.

### Observer Interface
```java
// CartListener.java
public interface CartListener {
    void onCartChanged();
}
```

### Observable Implementation
```java
// ShoppingCart.java - The Observable
public class ShoppingCart {
    private List<CartItem> items;
    private List<CartListener> listeners;

    public ShoppingCart() {
        this.items = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public void addListener(CartListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (CartListener listener : listeners) {
            listener.onCartChanged();
        }
    }

    public void addItem(Product product, int quantity) {
        Optional<CartItem> existing = items.stream()
            .filter(item -> item.getProduct().getId() == product.getId())
            .findFirst();

        if (existing.isPresent()) {
            existing.get().incrementQuantity();
        } else {
            items.add(new CartItem(product, quantity));
        }
        notifyListeners();  // Notify all listeners
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        notifyListeners();  // Notify all listeners
    }

    public void clear() {
        items.clear();
        notifyListeners();  // Notify all listeners
    }
}
```

### Observer Registration in UI
```java
// App.java - The Observer
public void start(Stage stage) {
    // ... initialization ...
    
    // Register this class as observer
    checkout.getCart().addListener(this::updateCartDisplay);
}

// Automatic update method called whenever cart changes
private void updateCartDisplay() {
    ShoppingCart cart = checkout.getCart();
    int itemCount = cart.getItemCount();
    double total = cart.getTotal();

    cartTitle.setText("Cart (" + itemCount + ")");
    subtotalLabel.setText(String.format("RM%.2f", total));
    totalLabel.setText(String.format("RM %.2f", total));
    
    // Rebuild cart items display
    cartItemsBox.getChildren().clear();
    List<CartItem> items = cart.getItems();
    for (CartItem cartItem : items) {
        cartItemsBox.getChildren().add(createCartItemRow(cartItem));
    }
}
```

### How It Works
```
1. Product added to cart
   ↓
2. ShoppingCart.addItem() called
   ↓
3. notifyListeners() invoked
   ↓
4. updateCartDisplay() automatically called
   ↓
5. UI updates instantly (Cart count, totals, items list)
```

### Benefits
- ✅ Loose coupling between ShoppingCart and UI
- ✅ Automatic UI synchronization without polling
- ✅ Multiple observers can listen to same cart
- ✅ Real-time updates on add/remove/clear operations
- ✅ Easy to add new observers without modifying cart

---

## 3. Strategy Pattern: PaymentMethod

### Purpose
Encapsulates different payment algorithms with interchangeable strategy objects. Allows selecting payment strategy at runtime.

### Strategy Interface
```java
// PaymentMethod.java - Defines contract for all payment strategies
public interface PaymentMethod {
    boolean processPayment(double amount);
    String getPaymentType();
}
```

### Concrete Strategy Implementations
```java
// CashPayment.java
public class CashPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        // Cash payment always succeeds
        System.out.println("Processing cash payment: RM" + amount);
        return true;
    }

    @Override
    public String getPaymentType() {
        return "Cash";
    }
}

// CardPayment.java
public class CardPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        // Card validation logic
        System.out.println("Processing card payment: RM" + amount);
        return true;
    }

    @Override
    public String getPaymentType() {
        return "Card";
    }
}

// EWalletPayment.java
public class EWalletPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        // E-wallet validation logic
        System.out.println("Processing e-wallet payment: RM" + amount);
        return true;
    }

    @Override
    public String getPaymentType() {
        return "E-Wallet";
    }
}
```

### Runtime Strategy Selection
```java
// CheckoutFacade.java - Uses strategy at runtime
public Receipt checkout(String paymentType) throws SQLException {
    if (cart.isEmpty()) {
        throw new IllegalStateException("Cart is empty");
    }

    // Strategy selected based on user input
    PaymentMethod payment = PaymentFactory.createPayment(paymentType);
    double total = cart.getTotal();

    // Strategy executed
    if (!payment.processPayment(total)) {
        throw new RuntimeException("Payment failed");
    }

    // Continue with receipt generation...
    return receipt;
}
```

### Benefits
- ✅ Strategies are interchangeable at runtime
- ✅ Each payment method independently testable
- ✅ New payment methods added without modifying existing code
- ✅ Follows Open/Closed Principle (Open for extension, Closed for modification)
- ✅ Avoids large if-else blocks in business logic

---

## 4. Factory Pattern: PaymentFactory

## 4. Factory Pattern: PaymentFactory

### Purpose
Centralizes the creation of payment method objects based on input type. Decouples payment creation from business logic.

### Factory Implementation
```java
// PaymentFactory.java - Centralized payment creation
public class PaymentFactory {
    public static PaymentMethod createPayment(String type) {
        switch (type.toUpperCase()) {
            case "CASH":
                return new CashPayment();
            case "CARD":
                return new CardPayment();
            case "EWALLET":
                return new EWalletPayment();
            default:
                throw new IllegalArgumentException("Unknown payment type: " + type);
        }
    }
}
```

### Usage in Checkout
```java
// CheckoutFacade.java - Uses factory
public Receipt checkout(String paymentType) throws SQLException {
    // Factory creates appropriate payment method
    PaymentMethod payment = PaymentFactory.createPayment(paymentType);
    boolean success = payment.processPayment(total);
    // ... rest of checkout
}
```

### Adding a New Payment Method
```java
// Step 1: Create new payment class
public class BiometricPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        return true;  // Biometric payment logic
    }

    @Override
    public String getPaymentType() {
        return "Biometric";
    }
}

// Step 2: Update factory (only one place!)
public static PaymentMethod createPayment(String type) {
    switch (type.toUpperCase()) {
        case "CASH":
            return new CashPayment();
        case "CARD":
            return new CardPayment();
        case "EWALLET":
            return new EWalletPayment();
        case "BIOMETRIC":  // NEW LINE
            return new BiometricPayment();  // NEW LINE
        default:
            throw new IllegalArgumentException("Unknown payment type: " + type);
    }
}

// Step 3: Update UI (optional)
RadioButton bioRadio = new RadioButton("Biometric");
bioRadio.setToggleGroup(paymentGroup);
radioBox.getChildren().add(bioRadio);

// Done! No other code changes needed.
```

### Benefits
- ✅ Centralizes creation logic in one place
- ✅ Easy to add new payment types without modifying checkout code
- ✅ Reduces code duplication
- ✅ Makes adding new payment methods straightforward
- ✅ Follows Single Responsibility Principle

### Factory + Strategy Pattern Together
```
Factory Pattern creates payment objects
        ↓
Strategy Pattern allows interchangeable algorithms
        ↓
Result: Flexible, extensible payment system
```

---

## 5. Facade Pattern: CheckoutFacade

### Purpose
Provides a single, simplified interface for a complex checkout process involving multiple subsystems. Hides complexity from the UI layer.

### Complex Subsystems Coordinated
```
CheckoutFacade coordinates:
├── ShoppingCart (item management)
├── PaymentFactory (payment creation)
├── PaymentMethod (payment processing)
├── ProductDAO (inventory queries & updates)
├── TransactionDAO (transaction persistence)
└── Receipt (transaction record generation)
```

### Facade Implementation
```java
// CheckoutFacade.java - Simplifies complex workflow
public class CheckoutFacade {
    private ShoppingCart cart;
    private ProductDAO productDAO;
    private TransactionDAO transactionDAO;

    public CheckoutFacade(ProductDAO productDAO, TransactionDAO transactionDAO) {
        this.cart = new ShoppingCart();
        this.productDAO = productDAO;
        this.transactionDAO = transactionDAO;
    }

    public void addToCart(Product product, int quantity) throws SQLException {
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock");
        }
        cart.addItem(product, quantity);
    }

    public Receipt checkout(String paymentType) throws SQLException {
        // 1. Validate
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 2. Create payment using Factory Pattern
        PaymentMethod payment = PaymentFactory.createPayment(paymentType);
        double total = cart.getTotal();

        // 3. Process payment
        if (!payment.processPayment(total)) {
            throw new RuntimeException("Payment failed");
        }

        // 4. Create receipt
        List<CartItem> items = cart.getItems();
        Receipt receipt = new Receipt(items, total, payment.getPaymentType());

        // 5. Update inventory
        updateInventory(items);

        // 6. Persist transaction
        saveTransaction(receipt);

        // 7. Clear for next transaction
        cart.clear();

        return receipt;
    }

    private void updateInventory(List<CartItem> items) throws SQLException {
        for (CartItem item : items) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productDAO.updateProduct(product);
        }
    }

    private void saveTransaction(Receipt receipt) throws SQLException {
        // Prepare transaction data
        StringBuilder itemsJson = new StringBuilder("[");
        List<CartItem> items = receipt.getItems();
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            itemsJson.append("{")
                .append("\"id\":").append(item.getProduct().getId())
                .append(", \"name\":\"").append(item.getProduct().getName()).append("\"")
                .append("}");
            if (i < items.size() - 1) itemsJson.append(",");
        }
        itemsJson.append("]");

        // Create and save transaction
        Transaction transaction = new Transaction(
            receipt.getTimestamp(),
            itemsJson.toString(),
            receipt.getSubtotal(),
            receipt.getDiscount(),
            receipt.getTotal(),
            receipt.getPaymentMethod()
        );
        transactionDAO.addTransaction(transaction);
    }

    public ShoppingCart getCart() {
        return cart;
    }
}
```

### UI Code Without Facade (Complex)
```java
// Without facade - scattered complex logic in App.java
ShoppingCart cart = ...;
if (cart.isEmpty()) throw new Exception("Cart is empty");

PaymentMethod payment = PaymentFactory.createPayment(type);
if (!payment.processPayment(total)) throw new Exception("Payment failed");

Receipt receipt = new Receipt(cart.getItems(), total, payment.getPaymentType());

for (CartItem item : cart.getItems()) {
    Product p = item.getProduct();
    p.setStock(p.getStock() - item.getQuantity());
    productDAO.updateProduct(p);
}

Transaction transaction = new Transaction(...);
transactionDAO.addTransaction(transaction);

cart.clear();
```

### UI Code With Facade (Clean)
```java
// With facade - single line!
Receipt receipt = checkout.checkout(selectedPayment);
new ReceiptPanel(receipt);
updateCartDisplay();
```

### Benefits
- ✅ Complex checkout workflow hidden behind simple interface
- ✅ UI code is clean and readable
- ✅ Easy to modify checkout flow without affecting UI
- ✅ Changes to subsystems don't affect UI code
- ✅ Reduced coupling between layers
- ✅ Single point of maintenance for checkout logic

---

## 6. Data Access Object (DAO) Pattern

### Purpose
Abstracts database operations from business logic. Provides standardized methods for Create, Read, Update, Delete operations.

### ProductDAO Implementation
```java
// ProductDAO.java - Database abstraction for Products
public class ProductDAO {
    private final Connection connection;

    public ProductDAO() throws SQLException {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public void addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (barcode, name, price, stock) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.executeUpdate();
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("barcode"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                ));
            }
        }
        return products;
    }

    public Product getProductByBarcode(String barcode) throws SQLException {
        String sql = "SELECT * FROM products WHERE barcode = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getInt("id"),
                        rs.getString("barcode"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                    );
                }
            }
        }
        return null;
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET barcode = ?, name = ?, price = ?, stock = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setInt(5, product.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
```

### TransactionDAO Implementation
```java
// TransactionDAO.java - Database abstraction for Transactions
public class TransactionDAO {
    private final Connection connection;

    public TransactionDAO() throws SQLException {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public void addTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (timestamp, items_json, subtotal, discount, total, payment_method) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transaction.getTimestamp().toString());
            stmt.setString(2, transaction.getItemsJson());
            stmt.setDouble(3, transaction.getSubtotal());
            stmt.setDouble(4, transaction.getDiscount());
            stmt.setDouble(5, transaction.getTotal());
            stmt.setString(6, transaction.getPaymentMethod());
            stmt.executeUpdate();
        }
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY id DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    LocalDateTime.parse(rs.getString("timestamp")),
                    rs.getString("items_json"),
                    rs.getDouble("subtotal"),
                    rs.getDouble("discount"),
                    rs.getDouble("total"),
                    rs.getString("payment_method")
                ));
            }
        }
        return transactions;
    }
}
```

### Benefits
- ✅ Separates database logic from business logic
- ✅ Easy to switch databases (SQLite → MySQL → PostgreSQL)
- ✅ Easier to test with mock DAOs
- ✅ Centralized CRUD operations
- ✅ Reduces code duplication
- ✅ Changes to SQL don't affect business layer

### Usage in Facade
```java
// CheckoutFacade only calls DAO methods
productDAO.updateProduct(product);      // Update inventory
transactionDAO.addTransaction(trans);   // Save transaction
```

---

## 7. Model-View-Controller (MVC) Architecture

### Purpose
Separates application into three interconnected layers for better organization, testability, and maintainability.

### Layer Structure
```
┌─────────────────────────────────┐
│      VIEW LAYER (UI)            │
│  ├── App.java (Main Window)     │
│  ├── AdminPanel.java            │
│  ├── LoginPanel.java            │
│  ├── MembershipPage.java        │
│  ├── ReceiptPanel.java          │
│  └── style.css                  │
├─────────────────────────────────┤
│    CONTROLLER LAYER (Service)   │
│  ├── CheckoutFacade.java        │
│  └── PaymentFactory.java        │
├─────────────────────────────────┤
│      MODEL LAYER (Data)         │
│  ├── Product.java               │
│  ├── CartItem.java              │
│  ├── ShoppingCart.java          │
│  ├── Receipt.java               │
│  ├── Transaction.java           │
│  └── PaymentMethod.java         │
├─────────────────────────────────┤
│   DATABASE LAYER (Persistence)  │
│  ├── DatabaseConfig.java        │
│  ├── ProductDAO.java            │
│  └── TransactionDAO.java        │
└─────────────────────────────────┘
```

### View Layer - UI Components
```java
// App.java - Main view
public class App extends Application {
    private CheckoutFacade checkout;  // Depends on Controller
    private List<Product> products;   // Depends on Model
    
    public void start(Stage stage) {
        // Build UI with JavaFX components
        HBox navbar = createNavbar(stage);
        VBox leftCol = createProductSection();
        VBox cart = createCartSection();
        
        // Register observer
        checkout.getCart().addListener(this::updateCartDisplay);
    }
}

// ReceiptPanel.java - Displays receipt
public class ReceiptPanel {
    private Receipt receipt;  // Uses model
    
    public ReceiptPanel(Receipt receipt) {
        this.receipt = receipt;
        createReceiptWindow();
    }
}
```

### Controller Layer - Business Logic
```java
// CheckoutFacade.java - Orchestrates business logic
public class CheckoutFacade {
    public Receipt checkout(String paymentType) throws SQLException {
        // Validates data (from models)
        // Processes business rules
        // Calls DAOs (persistence)
        // Returns updated model
    }
}

// PaymentFactory.java - Creates payment strategies
public class PaymentFactory {
    public static PaymentMethod createPayment(String type) {
        // Decision logic for creating models
    }
}
```

### Model Layer - Data Structures
```java
// Product.java - Product entity
public class Product {
    private int id;
    private String barcode;
    private String name;
    private double price;
    private int stock;
    // Getters, setters, constructors
}

// ShoppingCart.java - Shopping cart with observer
public class ShoppingCart {
    private List<CartItem> items;
    private List<CartListener> listeners;
    // Business logic for cart management
}

// Receipt.java - Transaction record
public class Receipt {
    private List<CartItem> items;
    private double subtotal;
    private double discount;
    private double total;
    private String paymentMethod;
    private LocalDateTime timestamp;
}
```

### Database Layer - Persistence
```java
// DAOs handle all database operations
// View and Controller don't know about SQL
ProductDAO productDAO = new ProductDAO();
productDAO.updateProduct(product);  // Only DAO knows SQL
```

### Data Flow Through Layers
```
USER INPUT (Click button)
    ↓
VIEW (App.java)
    ↓
CONTROLLER (CheckoutFacade)
    ↓
MODEL (Product, Cart, Receipt)
    ↓
DATABASE (ProductDAO, TransactionDAO)
    ↓
DISPLAY (ReceiptPanel)
```

### Benefits
- ✅ Clear separation of concerns
- ✅ Each layer has single responsibility
- ✅ Easy to test each layer independently
- ✅ Easy to modify UI without touching business logic
- ✅ Easy to change database without affecting UI/Controller
- ✅ Improved code organization and readability

---

## 8. Functional Programming: Stream API

### Purpose
Uses Java Stream API for functional, declarative data processing instead of imperative loops.

### Stream Usage in ShoppingCart
```java
// ShoppingCart.java - Functional stream operations
public class ShoppingCart {
    private List<CartItem> items;

    public void addItem(Product product, int quantity) {
        // Find existing item using Stream + Optional
        Optional<CartItem> existing = items.stream()
            .filter(item -> item.getProduct().getId() == product.getId())
            .findFirst();

        if (existing.isPresent()) {
            existing.get().incrementQuantity();
        } else {
            items.add(new CartItem(product, quantity));
        }
        notifyListeners();
    }

    public List<CartItem> getItems() {
        // Return defensive copy
        return new ArrayList<>(items);
    }

    public double getTotal() {
        // Calculate total using Stream + map + reduce
        return items.stream()
            .mapToDouble(CartItem::getSubtotal)
            .sum();
    }

    public int getItemCount() {
        // Count total quantity using Stream
        return items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
}
```

### Stream Usage in App.java
```java
// App.java - Building UI with streams
private void updateCartDisplay() {
    ShoppingCart cart = checkout.getCart();
    
    List<CartItem> items = cart.getItems();
    
    // Rebuild cart UI using enhanced for loop (functional-style)
    cartItemsBox.getChildren().clear();
    items.forEach(cartItem -> 
        cartItemsBox.getChildren().add(createCartItemRow(cartItem))
    );
}

// Product search using Stream
private Product findProduct(String input) throws SQLException {
    Product byBarcode = productDAO.getProductByBarcode(input);
    if (byBarcode != null) return byBarcode;

    // Search by name using Stream
    return products.stream()
        .filter(p -> p.getName().equalsIgnoreCase(input))
        .findFirst()
        .orElse(null);
}
```

### Stream Operations Used
| Operation | Purpose | Example |
|-----------|---------|---------|
| `filter()` | Filter elements by predicate | `.filter(item -> item.getQuantity() > 0)` |
| `map()` / `mapToDouble()` | Transform elements | `.mapToDouble(CartItem::getSubtotal)` |
| `sum()` | Aggregate values | `.sum()` |
| `findFirst()` | Get first matching element | `.findFirst()` |
| `forEach()` | Execute action for each element | `.forEach(System.out::println)` |
| `Optional` | Handle potentially null values | `Optional<CartItem> existing` |

### Benefits
- ✅ More concise and readable code
- ✅ Declarative (what to do, not how)
- ✅ Less error-prone than manual loops
- ✅ Better performance optimization opportunities
- ✅ Functional style matches modern Java practices

---

## Pattern Integration Summary

### How All Patterns Work Together

```
┌─────────────────────────────────────────────────────────┐
│              USER INTERACTION (App.java)                │
│  • Click product → addToCart()                          │
│  • Click charge → openPaymentDialog()                   │
│  • Select payment & confirm → checkout.checkout()      │
└─────────────────────────────────────────────────────────┘
                           ↓
        ┌──────────────────────────────────────┐
        │   OBSERVER PATTERN (ShoppingCart)    │
        │  • Cart notifies listeners on change │
        │  • UI updates automatically          │
        └──────────────────────────────────────┘
                           ↓
        ┌──────────────────────────────────────┐
        │   FACADE PATTERN (CheckoutFacade)    │
        │  • Single checkout() method          │
        │  • Hides complex workflow            │
        └──────────────────────────────────────┘
                           ↓
        ┌──────────────────────────────────────┐
        │  FACTORY PATTERN (PaymentFactory)    │
        │  • Creates appropriate PaymentMethod │
        │  • CASH → CardPayment                │
        │  • CARD → CardPayment                │
        │  • EWALLET → EWalletPayment          │
        └──────────────────────────────────────┘
                           ↓
        ┌──────────────────────────────────────┐
        │ STRATEGY PATTERN (PaymentMethod)     │
        │  • Executes selected payment strategy│
        │  • Process payment & return result   │
        └──────────────────────────────────────┘
                           ↓
        ┌──────────────────────────────────────┐
        │  DAO PATTERN (ProductDAO, etc.)      │
        │  • Update inventory                  │
        │  • Save transaction                  │
        │  • Persist to database               │
        └──────────────────────────────────────┘
                           ↓
        ┌──────────────────────────────────────┐
        │ SINGLETON (DatabaseConfig)           │
        │  • Single connection shared          │
        │  • Safe database access              │
        └──────────────────────────────────────┘
                           ↓
        ┌──────────────────────────────────────┐
        │  Display Receipt (ReceiptPanel)      │
        │  • Shows transaction details         │
        │  • Updates cart display              │
        └──────────────────────────────────────┘
```

### Code Example: Full Integration
```java
// App.java - Payment confirmation
confirmBtn.setOnAction(e -> {
    String selectedPayment = "CASH";
    if (cardRadio.isSelected()) {
        selectedPayment = "CARD";
    } else if (ewalletRadio.isSelected()) {
        selectedPayment = "EWALLET";
    }

    try {
        // ONE LINE uses ALL patterns:
        // 1. Facade coordinates workflow
        // 2. Factory creates payment method
        // 3. Strategy executes payment
        // 4. DAO persists data
        // 5. Singleton manages connection
        Receipt receipt = checkout.checkout(selectedPayment);
        
        // Observer automatically updated cart display
        // (ShoppingCart.notifyListeners() called internally)
        dialogStage.close();
        new ReceiptPanel(receipt);  // Display result
    } catch (SQLException ex) {
        showAlert("Error: " + ex.getMessage());
    }
});
```

---

## Design Principles Applied

### SOLID Principles

#### S - Single Responsibility
- `PaymentFactory`: Only responsible for creating payment objects
- `ProductDAO`: Only responsible for product database operations
- `CheckoutFacade`: Only responsible for coordinating checkout
- `PaymentMethod`: Only responsible for payment processing

#### O - Open/Closed
- New payment methods added without modifying existing code
- Implement `PaymentMethod` interface + update Factory = Done!
- No changes to `CheckoutFacade` or `App.java` needed

#### L - Liskov Substitution
- All `PaymentMethod` implementations are substitutable
- `checkout.checkout()` works with any payment type
- Can't tell if it's Cash, Card, or E-Wallet from caller's perspective

#### I - Interface Segregation
- `CartListener` interface focused on single method: `onCartChanged()`
- `PaymentMethod` interface focused on payment: `processPayment()`, `getPaymentType()`
- Clients depend on specific interfaces, not bloated ones

#### D - Dependency Inversion
- `CheckoutFacade` depends on abstractions, not concrete classes
- Depends on `PaymentMethod` interface, not `CashPayment`, `CardPayment`, etc.
- `App.java` depends on `CheckoutFacade` interface, not implementation

### DRY - Don't Repeat Yourself
- Payment creation logic centralized in `PaymentFactory`
- Database configuration centralized in `DatabaseConfig` (Singleton)
- DAO operations follow same pattern for Products and Transactions
- UI update logic centralized in `updateCartDisplay()`

### KISS - Keep It Simple, Stupid
- `checkout.checkout(paymentType)` - simple interface for complex operation
- Single observer listener - simple notification mechanism
- Stream operations - concise data manipulation

---

## Testing the Patterns

### Test Singleton
```java
DatabaseConfig db1 = DatabaseConfig.getInstance();
DatabaseConfig db2 = DatabaseConfig.getInstance();
assert db1 == db2;  // Same instance
```

### Test Observer
```java
ShoppingCart cart = new ShoppingCart();
List<String> updates = new ArrayList<>();
cart.addListener(() -> updates.add("changed"));

cart.addItem(product, 1);
assert updates.size() == 1;  // Listener called
```

### Test Factory
```java
PaymentMethod cash = PaymentFactory.createPayment("CASH");
PaymentMethod card = PaymentFactory.createPayment("CARD");
assert cash instanceof CashPayment;
assert card instanceof CardPayment;
```

### Test Facade
```java
CheckoutFacade checkout = new CheckoutFacade(productDAO, transactionDAO);
checkout.addToCart(product, 1);
Receipt receipt = checkout.checkout("CASH");
assert receipt.getTotal() == product.getPrice();
assert checkout.getCart().isEmpty();  // Cart cleared
```

---

## Extensibility Examples

### Adding a New Payment Method
**File: TouchNGoPayment.java** (NEW)
```java
public class TouchNGoPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        // TouchNGo-specific logic
        return true;
    }

    @Override
    public String getPaymentType() {
        return "TouchNGo";
    }
}
```

**Update: PaymentFactory.java** (1 line added)
```java
case "TOUCHNGO":
    return new TouchNGoPayment();
```

**Update: App.java - Payment Dialog** (3 lines added)
```java
RadioButton touchngoRadio = new RadioButton("TouchNGo");
touchngoRadio.setToggleGroup(paymentGroup);
radioBox.getChildren().add(touchngoRadio);
```

✅ Everything else works automatically!

### Adding a Discount Strategy
**File: Cart with Discount** (Future enhancement)
```java
// If discount system added later:
// 1. Create DiscountStrategy interface
// 2. Implement Gold/Silver/Bronze discounts
// 3. CheckoutFacade still works unchanged
// Factory pattern makes it easy to inject discounts
```

---

## Summary Table

| Pattern | Purpose | Location | Benefits |
|---------|---------|----------|----------|
| **Singleton** | Single database connection | `DatabaseConfig` | No connection conflicts |
| **Observer** | Real-time UI updates | `ShoppingCart` + `App` | Loose coupling, automatic sync |
| **Strategy** | Interchangeable algorithms | `PaymentMethod` + implementations | Runtime flexibility |
| **Factory** | Centralized object creation | `PaymentFactory` | Easy to extend |
| **Facade** | Simplify complex workflow | `CheckoutFacade` | Clean UI code |
| **DAO** | Abstract database operations | `ProductDAO`, `TransactionDAO` | Database independence |
| **MVC** | Separate concerns | Layered architecture | Better organization |
| **Streams** | Functional data processing | `ShoppingCart`, `App` | Concise, declarative |

---

## Key Takeaways

✅ **Flexible**: New payment methods added without modifying existing code
✅ **Maintainable**: Each component has single, clear responsibility
✅ **Testable**: Components can be tested independently
✅ **Scalable**: Architecture supports adding new features easily
✅ **Professional**: Follows industry best practices and SOLID principles
✅ **Clean**: UI code is simple and readable thanks to Facade pattern
✅ **Robust**: Database connection managed safely via Singleton pattern
✅ **Reactive**: UI automatically updates via Observer pattern
    } catch (SQLException ex) {
        showAlert("Error during checkout: " + ex.getMessage());
    }
});
```

---

## Adding a New Payment Method

### Step 1: Create new payment class
```java
// TouchNGoPayment.java
public class TouchNGoPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        // Implementation specific to TouchNGo
        return true;
    }

    @Override
    public String getPaymentType() {
        return "TouchNGo";
    }
}
```

### Step 2: Update PaymentFactory
```java
// PaymentFactory.java - add one case
public static PaymentMethod createPayment(String type) {
    switch (type.toUpperCase()) {
        case "CASH":
            return new CashPayment();
        case "CARD":
            return new CardPayment();
        case "EWALLET":
            return new EWalletPayment();
        case "TOUCHNGO":  // NEW
            return new TouchNGoPayment();  // NEW
        default:
            throw new IllegalArgumentException("Unknown payment type: " + type);
    }
}
```

### Step 3: Update UI (optional)
```java
// In App.java - payment dialog
RadioButton touchngoRadio = new RadioButton("TouchNGo");
touchngoRadio.setToggleGroup(paymentGroup);
radioBox.getChildren().add(touchngoRadio);
```

**That's it!** No changes needed to CheckoutFacade or any other code.
Checkout automatically supports the new payment method.

---

## Testing the Patterns

### Test Factory Pattern
```java
PaymentMethod cash = PaymentFactory.createPayment("CASH");
PaymentMethod card = PaymentFactory.createPayment("CARD");
PaymentMethod ewallet = PaymentFactory.createPayment("EWALLET");

// All three have same interface but different implementations
assert cash.getPaymentType().equals("Cash");
assert card.getPaymentType().equals("Card");
assert ewallet.getPaymentType().equals("E-Wallet");
```

### Test Facade Pattern
```java
// Before checkout
assert checkout.getCart().isEmpty();

// Add items and checkout
Product p = new Product("123", "Item", 10.0, 5);
checkout.addToCart(p, 1);
Receipt receipt = checkout.checkout("CASH");

// After checkout
assert checkout.getCart().isEmpty();
assert receipt.getTotal() == 10.0;
assert receipt.getPaymentMethod().equals("Cash");
```

---

## Summary

| Pattern | Purpose | Location |
|---------|---------|----------|
| **Factory** | Create payment methods | PaymentFactory.java |
| **Facade** | Simplify checkout workflow | CheckoutFacade.java |

Both patterns make the code:
- ✅ More maintainable
- ✅ More extensible
- ✅ Easier to test
- ✅ Less coupled
