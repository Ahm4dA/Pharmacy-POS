package Service;
import Model.DAO.CategoryDAO;
import Model.DAO.ProductDAO;
import Model.Entity.Category;
import Model.Entity.Product;
import View.BaseView;
import View.DialogWindow.AddCategoryView;
import View.DialogWindow.ManageCategoryView;
import View.DialogWindow.ManageExpiryView;
import View.InventoryView;
import View.LoginView;
import View.MainDashboardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService extends BaseService{
    InventoryView inventoryView;
    ManageCategoryView manageCategoryView;
    ManageExpiryView manageExpiryView;
    AddCategoryView addCategoryView;
    ProductDAO productDAO;
    CategoryDAO categoryDAO;

    public InventoryService(BaseView view)  {
        super(view);
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();
        inventoryView.updateCategoryList(categoryDAO.loadAll());
        refreshTable();
    }

    @Override
    public void loadDialogBoxes(){
        manageCategoryView = new ManageCategoryView((Frame) SwingUtilities.getWindowAncestor(view), true);
        addCategoryView = new AddCategoryView((Frame) SwingUtilities.getWindowAncestor(view), true);
        manageExpiryView = new ManageExpiryView((Frame) SwingUtilities.getWindowAncestor(view), true);
    }

    @Override
    protected void setChildReference() {
        inventoryView=((InventoryView)view);
    }

    @Override
    protected void checkViewType() {
        try{
        if (!(view instanceof InventoryView)) {
            throw new Exception("Expected Inventory View");
        }}
        catch(Exception ex){

        }
    }

    @Override
    protected void addListeners() {

        inventoryView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                inventoryView.setVisible(false);
                new MainDashboardService(new MainDashboardView("Main Dashboard"));
            }
        });

        inventoryView.getAddButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Adding add button action");
                List<Category> selectedCategories = inventoryView.getCategoryList().getSelectedValuesList();
                String code = inventoryView.getProductCodeField().getText();
                String name = inventoryView.getProductNameField().getText();
                String description = "This is a new product";
                int quantity;
                double price;

                try {
                    quantity = Integer.parseInt(inventoryView.getQuantityField().getText());
                    price = Double.parseDouble(inventoryView.getPriceField().getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(inventoryView, "Please enter valid numbers for quantity and price.");
                    return;
                }

                Product newProduct = new Product(code, name, description, quantity, price);
                for (Category category : selectedCategories) {
                    newProduct.addCategory(category);
                }

                try {
                    if (productDAO.productCodeExists(newProduct.getCode())) {
                        // Show confirmation dialog
                        int dialogResult = JOptionPane.showConfirmDialog(null, "Product code exists. Do you want to order more products?",
                                "Product Exists", JOptionPane.YES_NO_OPTION);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            // Asks for new price
                            String newPriceString = JOptionPane.showInputDialog("Enter new price or leave blank to keep the same:");
                            Double newPrice = newPriceString.isEmpty() ? newProduct.getPrice() : Double.parseDouble(newPriceString);
                            newProduct.setPrice(newPrice);

                            // Updates product quantity and price
                            productDAO.updateProductQuantityAndPrice(newProduct);
                        }
                    } else {
                        // Insert new product
                        productDAO.save(newProduct);
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                }

            }
        });


        inventoryView.getCategoryList().addListSelectionListener(e -> {
                refreshTable();
        });

        inventoryView.getManageCategoriesButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageCategoryView.getCategoryDropdown().removeAllItems();
                List<Category> categories = categoryDAO.loadAll();
                for(Category cat : categories){
                    manageCategoryView.getCategoryDropdown().addItem(cat);
                }
                manageCategoryView.getCategoryDropdown().setSelectedIndex(-1);
                manageCategoryView.setVisible(true);


            }
        });

        inventoryView.getAddCategoryButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCategoryView.getParentCategoryDropdown().removeAllItems();
                List<Category> categories = categoryDAO.loadAll();
                for(Category cat : categories){
                    addCategoryView.getParentCategoryDropdown().addItem(cat);
                }
                addCategoryView.getParentCategoryDropdown().setSelectedIndex(-1);

                addCategoryView.setVisible(true);
            }
        });

        addCategoryView.getAddButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String code = addCategoryView.getCategoryCodeField().getText();
                String name = addCategoryView.getCategoryNameField().getText();
                String description = addCategoryView.getCategoryDescriptionField().getText();

                Category newCategory = new Category(code, name, description);
                newCategory.setParentCategory((Category)addCategoryView.getParentCategoryDropdown().getSelectedItem());
                categoryDAO.save(newCategory);

                inventoryView.updateCategoryList(categoryDAO.loadAll());
                inventoryView.repaint();

            }
        });



        manageCategoryView.getDeleteButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Category selectedCategory = (Category) manageCategoryView.getCategoryDropdown().getSelectedItem();

                if (selectedCategory != null) {

                    int confirm = JOptionPane.showConfirmDialog(
                            manageCategoryView,
                            "Are you sure you want to delete the category: " + selectedCategory.getName() + "?",
                            "Delete Category",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        categoryDAO.delete(selectedCategory);
                        manageCategoryView.getCategoryDropdown().removeItem(selectedCategory);

                        manageCategoryView.getCategoryNameField().setText("");
                        manageCategoryView.getCategoryCodeField().setText("");
                        manageCategoryView.getCategoryDescriptionField().setText("");

                        JOptionPane.showMessageDialog(manageCategoryView, "Category deleted successfully.");

                    }
                } else {
                    JOptionPane.showMessageDialog(manageCategoryView, "Please select a category to delete.");
                }
            }
        });

        //FIX THIS
        manageCategoryView.getUpdateButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Category selectedCategory = (Category) manageCategoryView.getCategoryDropdown().getSelectedItem();
                if (selectedCategory != null) {
                    String oldCode = selectedCategory.getCode();

                    // Field enabled means field is being uppdated
                    if (manageCategoryView.getCategoryNameField().isEnabled()) {
                        selectedCategory.setName(manageCategoryView.getCategoryNameField().getText());
                    } else if (manageCategoryView.getCategoryCodeField().isEnabled()) {
                        selectedCategory.setCode(manageCategoryView.getCategoryCodeField().getText());
                    } else if (manageCategoryView.getCategoryDescriptionField().isEnabled()) {
                        selectedCategory.setDescription(manageCategoryView.getCategoryDescriptionField().getText());
                    }

                    // Update the category in the database
                    categoryDAO.update(selectedCategory, oldCode);
                    JOptionPane.showMessageDialog(manageCategoryView, "Category updated successfully.");
                    refreshCategoryDropdown();

                    // Reset the editability of all fields
                    resetFieldEditability();
                } else if (manageCategoryView.getCategoryDropdown().getSelectedIndex() == -1) {

                    manageCategoryView.getCategoryCodeField().setEnabled(true);
                    manageCategoryView.getCategoryNameField().setEnabled(true);
                    manageCategoryView.getCategoryDescriptionField().setEnabled(true);

                    String code= (manageCategoryView.getCategoryCodeField().getText());
                    String name = (manageCategoryView.getCategoryNameField().getText());
                    String description = (manageCategoryView.getCategoryDescriptionField().getText());
                    Category newCategory = new Category(code, name, description);

                    //categoryDAO.insert(newCategory);
                    JOptionPane.showMessageDialog(manageCategoryView, "New category created successfully.");
                } else {
                    JOptionPane.showMessageDialog(manageCategoryView, "Please select a category to update.");
                }
            }
        });

        inventoryView.getRemoveButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = inventoryView.getInventoryTable().getSelectedRow();
                if (selectedRow >= 0) {
                    // Ask for confirmation
                    int confirm = JOptionPane.showConfirmDialog(
                            inventoryView,
                            "Are you sure you want to delete the selected product?",
                            "Delete Product",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        String productCode = (String) inventoryView.getInventoryModel().getValueAt(selectedRow, 0);

                        productDAO.delete(productCode);
                        inventoryView.getInventoryModel().removeRow(selectedRow);

                        JOptionPane.showMessageDialog(inventoryView, "Product deleted successfully.");
                    }
                } else {
                    JOptionPane.showMessageDialog(inventoryView, "Please select 1 product to delete.");
                }
            }
        });

        manageCategoryView.getCategoryDropdown().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Category selectedCategory = (Category) manageCategoryView.getCategoryDropdown().getSelectedItem();
                if (selectedCategory != null) {
                    manageCategoryView.getCategoryNameField().setText(selectedCategory.getName());
                    manageCategoryView.getCategoryCodeField().setText(selectedCategory.getCode());
                    manageCategoryView.getCategoryDescriptionField().setText(selectedCategory.getDescription());
                }
            }
        });

        manageCategoryView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                inventoryView.updateCategoryList(categoryDAO.loadAll());
                inventoryView.repaint();
            }
        });


        inventoryView.getManageExpiryButton().addActionListener(e->{
            manageExpiryView.getProductDropdown().removeAllItems();
            List<Product> products = productDAO.getProductsByCategories(categoryDAO.loadAll());

            for(Product p: products){
                manageExpiryView.getProductDropdown().addItem(p);
            }

            updateManageExpiryView();
            manageExpiryView.setVisible(true);
        });

        manageExpiryView.getUpdateButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String productCode = ((Product)(manageExpiryView.getProductDropdown().getSelectedItem())).getCode();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    dateFormat.setLenient(false);
                    Date expiryDate = new Date(dateFormat.parse(manageExpiryView.getExpiryDateField().getText()).getTime());
                    int batchNum = Integer.parseInt(manageExpiryView.getBatchNumberField().getText());
                    String location = manageExpiryView.getLocationField().getText();

                    // Prepare the confirmation message
                    String confirmationMessage = "Are you sure you want to add the following expiry information?\n\n" +
                            "Product Code: " + productCode + "\n" +
                            "Batch Number: " + batchNum + "\n" +
                            "Expiry Date: " + manageExpiryView.getExpiryDateField().getText() + "\n" +
                            "Location: " + location;

                    // Show confirmation dialog
                    int confirmation = JOptionPane.showConfirmDialog(manageExpiryView, confirmationMessage,
                            "Confirm Expiry Information", JOptionPane.YES_NO_OPTION);

                    if (confirmation == JOptionPane.YES_OPTION) {
                        productDAO.insertExpiryInfo(productCode, batchNum, expiryDate, location);
                        JOptionPane.showMessageDialog(manageExpiryView, "Expiry information added successfully.");
                    }
                    updateManageExpiryView();

                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(manageExpiryView, "Error: Invalid date format. Please use dd-MM-yyyy.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(manageExpiryView, "Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        manageExpiryView.getDeleteButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Product selectedProduct = (Product) manageExpiryView.getProductDropdown().getSelectedItem();
                String batchNumber = manageExpiryView.getBatchNumberField().getText().trim();
                String location = manageExpiryView.getLocationField().getText().trim();

                if (selectedProduct != null) {
                    // Confirmation dialog
                    int confirmation = JOptionPane.showConfirmDialog(manageExpiryView,
                            "Are you sure you want to delete the expiry information for " +
                                    selectedProduct.getName() +
                                    (batchNumber.isEmpty() ? "" : ", Batch: " + batchNumber) +
                                    (location.isEmpty() ? "" : ", Location: " + location) + "?",
                            "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                    if (confirmation == JOptionPane.YES_OPTION) {
                        try {
                            productDAO.deleteExpiryInfo(selectedProduct.getCode(), batchNumber, location);
                            JOptionPane.showMessageDialog(manageExpiryView, "Expiry information deleted successfully.");
                            updateManageExpiryView();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(manageExpiryView, "Error: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(manageExpiryView, "Please select a product.");
                }
            }
        });




    }

    @Override
    public void refreshView() {
        inventoryView.getProductCodeField().setText("");
        inventoryView.getProductNameField().setText("");
        inventoryView.getQuantityField().setText("");
        inventoryView.getPriceField().setText("");
        inventoryView.getCategoryList().clearSelection();

    }

    private void resetFieldEditability() {
        manageCategoryView.getCategoryNameField().setEditable(true);
        manageCategoryView.getCategoryCodeField().setEditable(true);
        manageCategoryView.getCategoryDescriptionField().setEditable(true);
        manageCategoryView.getCategoryNameField().setEnabled(true);
        manageCategoryView.getCategoryCodeField().setEnabled(true);
        manageCategoryView.getCategoryDescriptionField().setEnabled(true);
        manageCategoryView.getCategoryNameField().setText("");
        manageCategoryView.getCategoryCodeField().setText("");
        manageCategoryView.getCategoryDescriptionField().setText("");
    }

    private void refreshCategoryDropdown() {
        manageCategoryView.getCategoryDropdown().removeAllItems();
        List<Category> categories = categoryDAO.loadAll();
        for (Category cat : categories) {
            manageCategoryView.getCategoryDropdown().addItem(cat);
        }
    }
    void refreshTable() {
        inventoryView.getInventoryModel().setRowCount(0);
        List<Product> selectedProducts = productDAO.getProductsByCategories(inventoryView.getCategoryList().getSelectedValuesList());
        for(Product p : selectedProducts) {
            inventoryView.addProductToTable(
                    p.getCode(),
                    p.getName(),
                    p.getStockQuantity(),
                    p.getPrice(),
                    p.getParentCategoryName()
            );
        }
    }


    private void updateManageExpiryView() {
        // Update upcoming expiries
        List<String> nearExpiryProducts = productDAO.getProductsNearExpiry();
        DefaultListModel<String> upcomingListModel = new DefaultListModel<>();
        nearExpiryProducts.forEach(upcomingListModel::addElement);
        manageExpiryView.getUpcomingExpiryList().setModel(upcomingListModel);

        // Update all expiries
        List<String> allExpiryProducts = productDAO.getAllExpiryInfo(); // This method needs to be implemented in ProductDAO
        DefaultListModel<String> allListModel = new DefaultListModel<>();
        allExpiryProducts.forEach(allListModel::addElement);
        manageExpiryView.getAllExpiryList().setModel(allListModel);
    }

}
