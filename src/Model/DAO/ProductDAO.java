package Model.DAO;

import Model.Entity.Category;
import Model.Entity.Product;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDAO{

    public void save(Product p) {
        String insertProductQuery = "INSERT INTO Products (productCode, productName, description, stockQuantity, price) VALUES (?,?,?,?,?)";
        String insertProductCategoryQuery = "INSERT INTO ProductCategories (productCode, categoryCode) VALUES (?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstProduct = conn.prepareStatement(insertProductQuery)) {

            pstProduct.setString(1, p.getCode());
            pstProduct.setString(2, p.getName());
            pstProduct.setString(3, p.getDescription());
            pstProduct.setInt(4, p.getStockQuantity());
            pstProduct.setDouble(5, p.getPrice());
            System.out.println(pstProduct);
            int affectedRows = pstProduct.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Creating product failed, no rows affected.");
                return;
            }


            try (PreparedStatement pstProductCategory = conn.prepareStatement(insertProductCategoryQuery)) {
                for (Category category : p.getCategories()) {
                    pstProductCategory.setString(1, p.getCode());
                    pstProductCategory.setString(2, category.getCode());
                    System.out.println(pstProductCategory);
                    pstProductCategory.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public List<Product> getProductsByCategories(List<Category> categories) {
        List<Product> products = new ArrayList<>();
        if (categories == null || categories.isEmpty()) {
            return products; // Return an empty list if no categories are provided.
        }

        // Make list of category IDs
        String categoryCodes = categories.stream()
                .map(Category::getCode)
                .map(code -> "'" + code + "'")
                .collect(Collectors.joining(","));

        String query = "SELECT DISTINCT p.* FROM Products p " +
                "JOIN ProductCategories pc ON p.productCode = pc.productCode " +
                "WHERE pc.categoryCode IN (" + categoryCodes + ")";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            System.out.println(pst);

            while (rs.next()) {
                products.add(new Product(
                        rs.getString("productCode"),
                        rs.getString("productName"),
                        rs.getString("description"),
                        rs.getInt("stockQuantity"),
                        rs.getDouble("price")
                ));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return products;
    }



    public void delete(Product p) {
        String deleteProductQuery = "DELETE FROM Products WHERE ProductCode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstProduct = conn.prepareStatement(deleteProductQuery)) {

            pstProduct.setString(1, p.getCode());
            int affectedRows = pstProduct.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Deleting product failed, no rows affected.");
            } else {
                System.out.println("Product deleted successfully.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void delete(String productCode) {
        String deleteProductQuery = "DELETE FROM Products WHERE ProductCode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstProduct = conn.prepareStatement(deleteProductQuery)) {

            pstProduct.setString(1, productCode);
            int affectedRows = pstProduct.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Deleting product failed, no rows affected.");
            } else {
                System.out.println("Product deleted successfully.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
