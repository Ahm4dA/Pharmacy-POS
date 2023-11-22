package View;

import Model.Entity.Category;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class InventoryView extends BaseView {

    private JTable inventoryTable;
    private JButton addButton, removeButton, updateButton, submitButton;
    private JTextField productCodeField, productNameField, quantityField, priceField;
    private JLabel productCodeLabel, productNameLabel, quantityLabel, priceLabel;

    JList<Category> categoryList;
    private DefaultListModel<Category> categoryListModel;
    private JScrollPane categoryScrollPane;

    public InventoryView(String title) {
        super(title);

    }


    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());

        inventoryTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);

        //Initializes category List
        categoryListModel= new DefaultListModel<>();
        ArrayList<Category> categoryData = new ArrayList<>();
        categoryData.add(new Category("123", "123", "123"));
        updateCategoryList(categoryData);
        categoryList = new JList<>(categoryListModel);
        categoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Allow multiple selections
        categoryScrollPane = new JScrollPane(categoryList);
        add(categoryScrollPane, BorderLayout.WEST);

        //Input panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // 5 rows, 2 columns, 10px gaps
        productCodeLabel = new JLabel("Product Code:");
        productCodeField = new JTextField();
        productNameLabel = new JLabel("Product Name:");
        productNameField = new JTextField();
        quantityLabel = new JLabel("Quantity:");
        quantityField = new JTextField();
        priceLabel = new JLabel("Price:");
        priceField = new JTextField();

       //Adding components to input panel
        inputPanel.add(productCodeLabel);
        inputPanel.add(productCodeField);
        inputPanel.add(productNameLabel);
        inputPanel.add(productNameField);
        inputPanel.add(quantityLabel);
        inputPanel.add(quantityField);
        inputPanel.add(priceLabel);
        inputPanel.add(priceField);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        updateButton = new JButton("Update");
        submitButton = new JButton("Submit");

        //Adding components to Button Panel
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(submitButton);

        // Adding panels to the main frame
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);}

    public JTable getInventoryTable() {
        return inventoryTable;
    }

    public JTextField getProductCodeField() { return productCodeField; }
    public JTextField getProductNameField() { return productNameField; }
    public JTextField getQuantityField() { return quantityField; }
    public JTextField getPriceField() { return priceField; }
    public JButton getAddButton() { return addButton; }
    public JButton getRemoveButton() { return removeButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getSubmitButton() { return submitButton; }

    public void updateCategoryList(ArrayList<Category> categories) {
        SwingUtilities.invokeLater(() -> {
            categoryListModel.clear();
            for (Category category : categories) {
                categoryListModel.addElement(category);
                for (Category subcategory : category.getSubcategories()) {
                    categoryListModel.addElement(subcategory);
                }
            }
        });
        }
    }