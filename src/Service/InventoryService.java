package Service;
import Model.DAO.CategoryDAO;
import Model.DAO.ProductDAO;
import Model.Entity.Category;
import Model.Entity.Product;
import View.BaseView;
import View.InventoryView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService extends BaseService{
    InventoryView inventoryView;
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
        System.out.println("Adding Listeners");
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
                System.out.println(newProduct);
                for (Category category : selectedCategories) {
                    newProduct.addCategory(category);
                    System.out.println(category.getName());
                }

                inventoryView.addProductToTable(code, name, quantity, price, selectedCategories.get(0).toString());
                productDAO.save(newProduct);
            }
        });

        inventoryView.getCategoryList().addListSelectionListener(e -> {
                refreshTable();
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
    void refreshTable(){
        inventoryView.getInventoryModel().setRowCount(0);
        List<Product> selectedProducts = productDAO.getProductsByCategories(inventoryView.getCategoryList().getSelectedValuesList());
        System.out.println("Found " + selectedProducts.size() + " products");
        for(Product p : selectedProducts){
            inventoryView.addProductToTable(p.getCode(), p.getName(), p.getStockQuantity(), p.getPrice(), p.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", ")));
        }

    }

}
