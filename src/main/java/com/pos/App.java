package com.pos;

import java.sql.SQLException;
import java.util.List;

import com.pos.db.DatabaseConfig;
import com.pos.db.ProductDAO;
import com.pos.db.TransactionDAO;
import com.pos.model.Product;
import com.pos.model.ShoppingCart;
import com.pos.service.CheckoutFacade;
import com.pos.ui.AdminPanel;
import com.pos.ui.LoginPanel;
import com.pos.ui.MembershipPage;
import com.pos.ui.ReceiptPanel;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
    private ProductDAO productDAO;
    private TransactionDAO transactionDAO;
    private CheckoutFacade checkout;
    private FlowPane productGrid;
    private List<Product> products;
    private Button registerBtn;
    private Label cartTitle;
    private Label subtotalLabel;
    private Label discountLabel;
    private Label totalLabel;
    private Button chargeBtn;
    private VBox cartItemsBox;
    private com.pos.model.Member activeMember = null; // Add this line

    @Override
    public void start(Stage stage) {
        // 1. Initialize Database
        try {
            DatabaseConfig.getInstance();
            productDAO = new ProductDAO();
            transactionDAO = new TransactionDAO();
            checkout = new CheckoutFacade(productDAO, transactionDAO);
            products = productDAO.getAllProducts();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add cart listener for real-time UI updates
        checkout.getCart().addListener(this::updateCartDisplay);

        // 2. Set Theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // --- 3. Build Main POS Layout ---
        HBox navbar = createNavbar(stage);

        VBox leftCol = new VBox(25);
        leftCol.setPadding(new Insets(30, 50, 30, 50));
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        productGrid = new FlowPane(15, 15);
        productGrid.setPadding(new Insets(10, 0, 0, 0));
        refreshProductGrid();

        leftCol.getChildren().addAll(createMembershipSection(), productGrid, createAddItemSection());

        VBox cart = createCartSection();

        HBox mainLayout = new HBox(20, leftCol, cart);
        mainLayout.setStyle("-fx-background-color: #F8FAFB;");

        BorderPane root = new BorderPane();
        root.setTop(navbar);
        root.setCenter(mainLayout);
        Scene shopScene = new Scene(root, 1200, 800);
        applyStylesheet(shopScene);

        MembershipPage membershipPage = new MembershipPage(() -> stage.setScene(shopScene));
        Scene membershipScene = new Scene(membershipPage.getView(), 1200, 800);
        applyStylesheet(membershipScene);

        // --- 4. Setup Login Page & Scene ---
        LoginPanel loginPanel = new LoginPanel(
            () -> stage.setScene(shopScene), // Back button returns to Shop
            (success) -> {
                if (success) {
                    AdminPanel admin = new AdminPanel(this::refreshProducts);
                    admin.show();
                    stage.setScene(shopScene); // Stay on Shop screen after Admin launches
                }
            }
        );
        Scene loginScene = new Scene(loginPanel.getView(), 1200, 800);
        applyStylesheet(loginScene);

        // --- 5. Link Admin Button to Login Scene ---
        Button adminBtn = (Button) navbar.getChildren().get(2);
        adminBtn.setOnAction(e -> stage.setScene(loginScene));

        if (registerBtn != null) {
        registerBtn.setOnAction(e -> stage.setScene(membershipScene));
    }

        stage.setTitle("Group 6 Shop");
        stage.setScene(shopScene);
        stage.show();
    }

    // --- Navigation & Refresh Helpers ---

    private void refreshProducts() {
        try {
            products = productDAO.getAllProducts();
            refreshProductGrid();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshProductGrid() {
        productGrid.getChildren().clear();
        for (Product product : products) {
            productGrid.getChildren().add(createProductCard(product));
        }
    }

    private void applyStylesheet(Scene scene) {
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Stylesheet error: " + e.getMessage());
        }
    }

    private void updateCartDisplay() {
        ShoppingCart cart = checkout.getCart();
        int itemCount = cart.getItemCount();
        double subtotal = cart.getTotal(); 

        // --- CALCULATION LOGIC ---
        double discountAmount = 0.0;
        if (activeMember != null) {
            discountAmount = subtotal * 0.10; // 10% discount
        }
        
        // REMOVE THE 'double' WORD HERE
        double finalPrice = subtotal - discountAmount;

        // Update totals and buttons
        cartTitle.setText("Cart (" + itemCount + ")");
        subtotalLabel.setText(String.format("RM %.2f", subtotal));
        discountLabel.setText(String.format("-RM %.2f", 0.0));
        discountLabel.setText(String.format("-RM %.2f", discountAmount));   
        totalLabel.setText(String.format("RM %.2f", finalPrice));
        
        // Rebuild cart items list
        cartItemsBox.getChildren().clear();

        List<com.pos.model.CartItem> items = cart.getItems();
        if (items.isEmpty()) {
            Label emptyLabel = new Label("No items in cart");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            cartItemsBox.getChildren().add(emptyLabel);
        } else {
            for (com.pos.model.CartItem cartItem : items) {
                cartItemsBox.getChildren().add(createCartItemRow(cartItem));
            }
        }
    }

    private HBox createCartItemRow(com.pos.model.CartItem cartItem) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: #DDD; -fx-border-radius: 2; -fx-background-color: white;");
        row.setAlignment(Pos.CENTER_LEFT);

        // Product name
        Label nameLabel = new Label(cartItem.getProduct().getName());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Quantity controls
        Button minusBtn = new Button("-");
        minusBtn.setPrefWidth(40);
        minusBtn.setStyle("-fx-font-size: 12px;");
        minusBtn.setOnAction(e -> handleDecreaseQuantity(cartItem));

        Label qtyLabel = new Label(String.valueOf(cartItem.getQuantity()));
        qtyLabel.setStyle("-fx-font-size: 12px; -fx-min-width: 30;");
        qtyLabel.setAlignment(Pos.CENTER);

        Button plusBtn = new Button("+");
        plusBtn.setPrefWidth(40);
        plusBtn.setStyle("-fx-font-size: 12px;");
        plusBtn.setOnAction(e -> handleIncreaseQuantity(cartItem));

        // Price
        Label priceLabel = new Label(String.format("RM%.2f", cartItem.getSubtotal()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #008B8B; -fx-font-weight: bold;");
        priceLabel.setPrefWidth(70);

        row.getChildren().addAll(nameLabel, minusBtn, qtyLabel, plusBtn, priceLabel);
        return row;
    }

    private void handleIncreaseQuantity(com.pos.model.CartItem cartItem) {
        if (cartItem.canIncrementQuantity()) {
            cartItem.incrementQuantity();
            updateCartDisplay();
        } else {
            showAlert("Cannot add more. Maximum stock for " + cartItem.getProduct().getName() + " is " + cartItem.getProduct().getStock());
        }
    }

    private void handleDecreaseQuantity(com.pos.model.CartItem cartItem) {
        if (cartItem.getQuantity() > 1) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            updateCartDisplay();
        } else {
            checkout.getCart().removeItem(cartItem);
            updateCartDisplay();
        }
    }

    private void openPaymentDialog() {
        if (checkout.getCart().isEmpty()) {
            showAlert("Cart is empty. Please add items before checking out.");
            return;
        }

        Stage dialogStage = new Stage();
        VBox dialogBox = new VBox(15);
        dialogBox.setPadding(new Insets(20));
        dialogBox.setAlignment(Pos.CENTER);

        Label title = new Label("Select Payment Method");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ToggleGroup paymentGroup = new ToggleGroup();

        RadioButton cashRadio = new RadioButton("Cash");
        cashRadio.setToggleGroup(paymentGroup);
        cashRadio.setSelected(true);

        RadioButton cardRadio = new RadioButton("Card");
        cardRadio.setToggleGroup(paymentGroup);

        RadioButton ewalletRadio = new RadioButton("E-Wallet");
        ewalletRadio.setToggleGroup(paymentGroup);

        VBox radioBox = new VBox(10);
        radioBox.getChildren().addAll(cashRadio, cardRadio, ewalletRadio);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button confirmBtn = new Button("Confirm");
        confirmBtn.setOnAction(e -> {
            String selectedPayment = "CASH";
            if (cardRadio.isSelected()) {
                selectedPayment = "CARD";
            } else if (ewalletRadio.isSelected()) {
                selectedPayment = "EWALLET";
            }

            try {
                com.pos.model.Receipt receipt = checkout.checkout(selectedPayment);
                dialogStage.close();

                new ReceiptPanel(receipt);
                refreshProducts();  // Refresh products to update stock display
                updateCartDisplay();
            } catch (SQLException ex) {
                showAlert("Error during checkout: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().addAll(confirmBtn, cancelBtn);

        dialogBox.getChildren().addAll(title, radioBox, buttonBox);

        Scene dialogScene = new Scene(dialogBox, 300, 250);
        dialogStage.setScene(dialogScene);
        dialogStage.setTitle("Payment");
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notice");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- UI Component Helpers ---

    private HBox createNavbar(Stage stage) {
        HBox navbar = new HBox();
        navbar.setPadding(new Insets(15, 30, 15, 30));
        navbar.setStyle("-fx-background-color: white; -fx-border-color: #EEE; -fx-border-width:0 0 1 0;");
        
        Label logo = new Label("Group 6 Shop");
        logo.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button adminBtn = new Button("Admin Panel");
        adminBtn.getStyleClass().add("teal-button");

        navbar.getChildren().addAll(logo, spacer, adminBtn);
        return navbar;
    }

    private VBox createMembershipSection() {
        VBox box = new VBox(15);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(25));

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(new Label("👤"), new Label("Membership (optional)"));

        HBox searchBox = new HBox(10);
        TextField memberSearch = new TextField();
        memberSearch.setPromptText("Type member name, email, or ID...");
        HBox.setHgrow(memberSearch, Priority.ALWAYS);
        Button findBtn = new Button("Find");
        findBtn.getStyleClass().add("teal-button");
        searchBox.getChildren().addAll(memberSearch, findBtn);

        findBtn.setOnAction(e -> {
        String input = memberSearch.getText().trim();
        if (input.isEmpty()) return;

        try {
            com.pos.db.MembersDAO membersDAO = new com.pos.db.MembersDAO();
            activeMember = membersDAO.findMember(input);

            if (activeMember != null) {
                memberSearch.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px;");
                updateCartDisplay(); // Refresh the numbers
                showAlert("Member Found: Welcome back, " + activeMember.getName() + "!");
            } else {
                memberSearch.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                activeMember = null;
                updateCartDisplay();
                showAlert("Member not found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    });

        Label helpText = new Label("Try ali@example.com (Gold, 10% off).");
        helpText.setStyle("-fx-text-fill: #777; -fx-font-size: 13px;");

        registerBtn = new Button("New Member");
        registerBtn.getStyleClass().add("button-outline");
        box.getChildren().addAll(titleBox, searchBox, helpText, registerBtn);

        return box;
    }

    private VBox createAddItemSection() {
        VBox box = new VBox(15);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(25));

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(new Label("|||"), new Label("Add item - scan barcode or type item name"));

        HBox addBox = new HBox(10);
        TextField itemInput = new TextField();
        itemInput.setPromptText("e.g. 8801234500011 or Croissant");
        HBox.setHgrow(itemInput, Priority.ALWAYS);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("teal-button");

        addBtn.setOnAction(e -> {
            String input = itemInput.getText().trim();
            if (input.isEmpty()) {
                showAlert("Please enter a barcode or product name");
                return;
            }

            try {
                Product product = findProduct(input);
                if (product == null) {
                    showAlert("Product not found: " + input);
                    return;
                }

                if (product.getStock() <= 0) {
                    showAlert("Product out of stock: " + product.getName());
                    return;
                }

                checkout.addToCart(product, 1);
                itemInput.clear();
            } catch (SQLException ex) {
                showAlert("Error adding to cart: " + ex.getMessage());
            }
        });

        addBox.getChildren().addAll(itemInput, addBtn);
        box.getChildren().addAll(titleBox, addBox);
        return box;
    }

    private Product findProduct(String input) throws SQLException {
        // Try barcode first
        Product byBarcode = productDAO.getProductByBarcode(input);
        if (byBarcode != null) {
            return byBarcode;
        }

        // Try product name (case-insensitive)
        for (Product p : products) {
            if (p.getName().equalsIgnoreCase(input)) {
                return p;
            }
        }

        return null;
    }

    private VBox createCartSection() {
        VBox cart = new VBox(15);
        cart.getStyleClass().add("card");
        cart.setPrefWidth(380);
        cart.setPadding(new Insets(25));

        cartTitle = new Label("Cart (0)");
        cartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Cart items list
        cartItemsBox = new VBox(8);
        cartItemsBox.setPadding(new Insets(10));
        cartItemsBox.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #EEE; -fx-border-radius: 4;");

        ScrollPane scrollPane = new ScrollPane(cartItemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-control-inner-background: #FAFAFA;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        cart.getChildren().addAll(cartTitle, scrollPane, createCartSummary(), createChargeButton());
        return cart;
    }

    private Button createChargeButton() {
        chargeBtn = new Button("Charge RM 0.00");
        chargeBtn.getStyleClass().add("button-charge");
        chargeBtn.setMaxWidth(Double.MAX_VALUE);
        chargeBtn.setOnAction(e -> openPaymentDialog());
        return chargeBtn;
    }

    private VBox createCartSummary() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 0, 10, 0));

        subtotalLabel = new Label("RM0.00");
        HBox subtotal = new HBox(new Label("Subtotal"), new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, subtotalLabel);

        discountLabel = new Label("-RM 0.00");
        discountLabel.setStyle("-fx-text-fill: #777;");
        HBox discount = new HBox(new Label("Discount"), new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, discountLabel);

        totalLabel = new Label("RM 0.00");
        totalLabel.setStyle("-fx-font-size:20px; -fx-font-weight: bold; -fx-text-fill: #008B8B;");
        HBox total = new HBox(new Label("Total") {{ setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); }},
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, totalLabel);

        box.getChildren().addAll(subtotal, discount, new Separator(), total);
        return box;
    }

    private VBox createProductCard(Product product) {
        VBox box = new VBox(8);
        box.getStyleClass().add("product-card");
        box.setStyle("-fx-cursor: hand;");

        Label barcode = new Label(product.getBarcode());
        barcode.setStyle("-fx-font-size:11px; -fx-text-fill: #AAA;");

        Label name = new Label(product.getName());
        name.setStyle("-fx-font-size: 11px; -fx-text-fill: #AAA;");
        name.setWrapText(true);

        HBox bottom = new HBox();
        bottom.setAlignment(Pos.BOTTOM_LEFT);
        Label price = new Label(String.format("RM %.2f", product.getPrice()));
        price.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #008B8B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        String stockText = product.getStock() > 0 ? product.getStock() + " LEFT" : "OUT OF STOCK";
        Label stock = new Label(stockText);
        String stockStyle = product.getStock() > 0 
            ? "-fx-font-size: 12px; -fx-text-fill: #AAA;" 
            : "-fx-font-size: 12px; -fx-text-fill: #FF6B6B; -fx-font-weight: bold;";
        stock.setStyle(stockStyle);

        bottom.getChildren().addAll(price, spacer, stock);
        box.getChildren().addAll(barcode, name, bottom);

        // Add click handler to add to cart
        box.setOnMouseClicked(e -> {
            if (product.getStock() <= 0) {
                showAlert("Product out of stock: " + product.getName());
                return;
            }

            try {
                checkout.addToCart(product, 1);
            } catch (SQLException ex) {
                showAlert("Error adding to cart: " + ex.getMessage());
            }
        });

        return box;
    }

    public static void main(String[] args) { launch(args); }
}