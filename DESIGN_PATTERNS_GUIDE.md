# Design Patterns - Implementation Guide

## Factory Pattern: PaymentFactory

### How It Works
The PaymentFactory creates different payment method objects based on input type:

```java
// In CheckoutFacade.java - checkout() method
PaymentMethod payment = PaymentFactory.createPayment(paymentType);
boolean success = payment.processPayment(total);
```

### Factory Implementation
```java
// PaymentFactory.java
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

### Payment Strategy Interface
```java
// PaymentMethod.java
public interface PaymentMethod {
    boolean processPayment(double amount);
    String getPaymentType();
}
```

### Concrete Payment Implementations
```java
// CashPayment.java
public class CashPayment implements PaymentMethod {
    @Override
    public boolean processPayment(double amount) {
        return true; // Always succeeds
    }

    @Override
    public String getPaymentType() {
        return "Cash";
    }
}

// Similar for CardPayment and EWalletPayment
```

### Benefits
- ✅ Adding new payment type only requires implementing PaymentMethod interface
- ✅ Checkout code doesn't change when adding new payment methods
- ✅ Easy to extend without modifying existing code

---

## Facade Pattern: CheckoutFacade

### How It Works
The CheckoutFacade provides a single, simplified interface for the entire checkout process:

```java
// In App.java - from payment dialog
Receipt receipt = checkout.checkout(selectedPayment);
```

### What Happens Behind the Scenes
The CheckoutFacade coordinates multiple operations:

```java
// CheckoutFacade.java
public Receipt checkout(String paymentType) throws SQLException {
    // 1. Validate cart
    if (cart.isEmpty()) {
        throw new IllegalStateException("Cart is empty");
    }

    // 2. Get payment method (uses Factory Pattern!)
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

    // 6. Save to database
    saveTransaction(receipt);

    // 7. Clear cart for next transaction
    cart.clear();

    return receipt;
}
```

### Subsystems Coordinated
1. **ShoppingCart** - Item management
2. **PaymentFactory** - Payment creation
3. **ProductDAO** - Inventory updates
4. **TransactionDAO** - Persistence
5. **Receipt** - Transaction record generation

### Before Facade (Complex)
```java
// Without facade - complex scattered code in UI:
ShoppingCart cart = ...;
if (cart.isEmpty()) throw new Exception(...);

PaymentMethod payment = PaymentFactory.createPayment(type);
if (!payment.processPayment(cart.getTotal())) throw new Exception(...);

Receipt receipt = new Receipt(cart.getItems(), ...);

for (CartItem item : cart.getItems()) {
    Product p = item.getProduct();
    p.setStock(p.getStock() - item.getQuantity());
    productDAO.updateProduct(p);
}

Transaction transaction = new Transaction(...);
transactionDAO.addTransaction(transaction);

cart.clear();
```

### After Facade (Clean)
```java
// With facade - single line handles everything
Receipt receipt = checkout.checkout(paymentType);
```

### Benefits
- ✅ UI code is clean and simple
- ✅ Checkout logic is in one place (CheckoutFacade)
- ✅ Easy to modify checkout flow without affecting UI
- ✅ Complex multi-step process becomes single method call
- ✅ Reduced coupling between UI and business logic

---

## How Patterns Work Together

### In the Checkout Flow
```
User clicks "Confirm" in payment dialog
    ↓
App calls: checkout.checkout(selectedPayment)
    ↓
CheckoutFacade.checkout() method:
    1. Validates cart
    2. Calls PaymentFactory.createPayment(type)  ← Factory Pattern
    3. Creates appropriate PaymentMethod object
    4. Processes payment
    5. Updates inventory
    6. Saves transaction
    7. Returns receipt
    ↓
UI receives receipt
    ↓
ReceiptPanel displays transaction details
```

### Code Example: Integration
```java
// In App.java - payment dialog confirmation
confirmBtn.setOnAction(e -> {
    String selectedPayment = "CASH";
    if (cardRadio.isSelected()) {
        selectedPayment = "CARD";
    } else if (ewalletRadio.isSelected()) {
        selectedPayment = "EWALLET";
    }

    try {
        // This single line uses BOTH patterns:
        // 1. CheckoutFacade (Facade) orchestrates the whole process
        // 2. PaymentFactory (Factory) creates the right payment method inside
        Receipt receipt = checkout.checkout(selectedPayment);
        
        dialogStage.close();
        new ReceiptPanel(receipt);
        updateCartDisplay();
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
