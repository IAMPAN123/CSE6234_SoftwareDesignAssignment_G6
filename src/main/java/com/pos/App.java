package com.pos;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        
        //Top Navbar
        HBox navbar = new HBox();
        navbar.setPadding(new Insets(15,30,15,30));
        navbar.setStyle("-fx-background-color: white; -fx-border-color: #EEE; -fx-border-width:0 0 1 0;");
        Label logo = new Label("Group 6 Shop");
        logo.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        navbar.getChildren().add(logo);

        // Left Column
        VBox leftCol = new VBox(25);
        leftCol.setPadding(new Insets(30, 50, 30, 50));
        HBox.setHgrow(leftCol, Priority.ALWAYS);
        
        VBox membership = createMembershipSection();

        VBox addItem = createAddItemSection();

        

        //Product Grid
        FlowPane productGrid = new FlowPane(15,15);
        productGrid.setPadding(new Insets(10, 0, 0, 0));

        productGrid.getChildren().addAll(
            createComplexProductCard("8801234500011", "Espresso Beans 250g", "RM 18.50", "24 LEFT"),
            createComplexProductCard("8801234500028", "Oat Milk 1L", "RM 7.90", "8 LEFT"),
            createComplexProductCard("8801234500035", "Croissant", "RM 4.20", "30 LEFT"),
            createComplexProductCard("8801234500042", "Dark Chocolate Bar", "RM 6.50", "5 LEFT"),
            createComplexProductCard("8801234500059", "Sparkling Water 500ml", "RM 3.00", "50 LEFT"),
            createComplexProductCard("8801234500066", "Avocado", "RM 4.50", "18 LEFT")
        );

        leftCol.getChildren().addAll(membership, productGrid,addItem);
        

        // Right Column (Cart)
        VBox cart = new VBox(20);
        cart.getStyleClass().add("card");
        cart.setPrefWidth(380);
        cart.setPadding(new Insets(25));

        Label cartTitle = new Label("Cart (0)");
        cartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox cartSummary = createCartSummary();
        
        Button chargeBtn = new Button("Charge RM 0.00");
        chargeBtn.getStyleClass().add("button-charge");
        chargeBtn.setMaxWidth(Double.MAX_VALUE);

        cart.getChildren().addAll( cartTitle, spacer, cartSummary, chargeBtn);
        
        //Main Layout
        HBox mainLayout = new HBox(20, leftCol, cart);
        mainLayout.setStyle("-fx-background-color: #F8FAFB;");

        BorderPane root = new BorderPane();
        root.setTop(navbar);
        root.setCenter(mainLayout);

        Scene scene = new Scene(root, 1200, 800);
        try{ 
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Stylesheet not found: " + e.getMessage());
        }

        stage.setTitle("Group 6 Shop");
        stage.setScene(scene);
        stage.show();


        

    }

    

    //helper method
    

    private VBox createMembershipSection(){
        VBox box = new VBox(15);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(25));

        //Membership
        HBox membershipTitleBox = new HBox(10);
        membershipTitleBox.setAlignment(Pos.CENTER_LEFT);
        Label userIcon = new Label ("👤");
        Label membershipLabel = new Label("Membership (optional)");
        membershipTitleBox.getChildren().addAll (userIcon, membershipLabel);

        //Search Field Box
        HBox searchFieldHBox = new HBox(10);
        TextField memberSearch = new TextField();
        memberSearch.setPromptText("Type member name, email, or ID...");
        HBox.setHgrow(memberSearch, Priority.ALWAYS);

        Button findBtn = new Button("Find");
        findBtn.getStyleClass().add("teal-button");
        searchFieldHBox.getChildren().addAll(memberSearch, findBtn);

        Label examplesLabel = new Label("Try ali@example.com (Gold, 10% off).");
        examplesLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 13px;");

        box.getChildren().addAll(membershipTitleBox,searchFieldHBox,examplesLabel);
        return box;
    }

    private VBox createAddItemSection() {
        VBox box = new VBox(15);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(25));

        //Add item
        HBox addItemTitleBox = new HBox(10);
        addItemTitleBox.setAlignment(Pos.CENTER_LEFT);
        Label barcodeIcon = new Label("|||");
        Label addItemLabel = new Label("Add item - scan barcode or type item name");
        addItemTitleBox.getChildren().addAll(barcodeIcon, addItemLabel);

        HBox addFieldHBox = new HBox(10);
        TextField itemInput = new TextField();
        itemInput.setPromptText("e.g. 8801234500011 or Croissant");
        HBox.setHgrow(itemInput, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("teal-button");
        addFieldHBox.getChildren().addAll(itemInput, addBtn);

        box.getChildren().addAll(addItemTitleBox, addFieldHBox);
        return box;

    }

    private VBox createCartSummary(){
        VBox box = new VBox(10);
        box.setPadding(new Insets(10,0,10,0));

        HBox subtotalBox = new HBox();
        Label subLabel = new Label("Subtotal");
        Label subPrice = new Label ("RM0.00");
        subtotalBox.getChildren().addAll(subLabel, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, subPrice);
        
        HBox discountBox = new HBox();
        Label disLabel = new Label("Discount");
        Label disPrice = new Label("-RM 0.00");
        disPrice.setStyle("-fx-text-fill: #777;");
        discountBox.getChildren().addAll(disLabel, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, disPrice);

        Separator sep = new Separator();
        sep.setPadding(new Insets (5, 0, 5, 0));

        HBox totalBox = new HBox();
        Label totalLabel = new Label("Total");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label totalPrice = new Label ("RM 0.00");
        totalPrice.setStyle("-fx-font-size:20px; -fx-font-weight: bold; -fx-text-fill: #008B8B;");
        totalBox.getChildren().addAll(totalLabel, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, totalPrice);

        box.getChildren().addAll(subtotalBox, discountBox, sep, totalBox);
        return box;
    }

    private VBox createComplexProductCard(String barcode, String name, String price, String stock){
        VBox box = new VBox(8);
        box.getStyleClass().add("product-card");

        //Barcode
        Label barcodeLabel = new Label(barcode);
        barcodeLabel.setStyle("-fx-font-size:11px; -fx-text-fill: #AAA;");

        //Product Name
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #AAA;");
        nameLabel.setWrapText(true);

        //Price and stock
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.BOTTOM_LEFT);

        Label priceLabel = new Label(price);
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #008B8B;");

        Region bottomSpacer = new Region ();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        Label stockLabel = new Label(stock);
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #AAA;");

        if (stock.contains("LEFT")&& stock.length() < 9) {
            stockLabel.getStyleClass().add("stock-critical");
        } 

        bottomRow.getChildren().addAll(priceLabel, bottomSpacer, stockLabel);

        box.getChildren().addAll(barcodeLabel, nameLabel, bottomRow);
        return box;
    }
    public static void main(String[] args) { launch(args); }
}