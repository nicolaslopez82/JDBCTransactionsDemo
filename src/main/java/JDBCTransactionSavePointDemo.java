import java.sql.*;

/**
 *
 * JDBC Transaction with Save Point Demo Program
 *
 * Using Save Points in a Transaction
 * The JDBC API provides the Connection.setSavepoint() method that marks a point to which the transaction can be rolled back.
 * The rollback() method is overloaded to takes a save point as its argument:
 * connection.rollback(savepoint)
 *
 * This allows us to undo only changes after the save point in case something wrong happen. The changes before the save point
 * are still committed. The following workflow helps you understand how save points are used in a transaction:
 *
 * In the following program, the transaction consists of the following statement:
 * - insert a new product to the table products.
 * - insert a new order to the table orders.
 * - update total amount in the monthly sales.
 *
 * In case the amount of the new order cannot help monthly sales > 10,000, the transaction is rolled back to the point where the new product was inserted.
 * Here’s the code of the program that shows how to use save point in a transaction with JDBC:
 *
 *
 * NOTE:
 * The JDBC API provides the Connection.releaseSavepoint(savepoint) method that removes the specified save point from the current transaction.
 * A save point has been released become invalid and cannot be rolled back to. Any attempt to roll back the transaction to a released save point
 * causes a SQLException.
 *
 * A save point is automatically released and becomes invalid when the transaction is committed or when the entire transaction is rolled back.
 *
 * Created by NicolasLopez on 24/10/2017.
 */
public class JDBCTransactionSavePointDemo {

    private String dbURL = "jdbc:mysql://localhost:3306/jdbctransactiondemo";
    private String user = "root";
    private String password = "";
    private Connection conn;

    public void connect() {
        try {
            conn = DriverManager.getConnection(dbURL, user, password);
            System.out.println("Connected.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            conn.close();
            System.out.println("Closed.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void saveOrder(String newProductName, float newProductPrice,
                          int productId, Date orderDate, float orderAmount, int reportMonth) {

        PreparedStatement productStatement = null;
        PreparedStatement orderStatement = null;
        PreparedStatement saleStatement = null;
        PreparedStatement getTotalStatement = null;

        try {

            conn.setAutoCommit(false);

            String sqlSaveProduct = "insert into products (product_name, price)";
            sqlSaveProduct += " values (?, ?)";

            productStatement = conn.prepareStatement(sqlSaveProduct);
            productStatement.setString(1, newProductName);
            productStatement.setFloat(2, newProductPrice);

            productStatement.executeUpdate();

            // productStatement are executed successfully till this point:
            Savepoint savepoint = conn.setSavepoint();

            String sqlSaveOrder = "insert into orders (product_id, order_date, amount)";
            sqlSaveOrder += " values (?, ?, ?)";

            orderStatement = conn.prepareStatement(sqlSaveOrder);
            orderStatement.setInt(1, productId);
            orderStatement.setDate(2, orderDate);
            orderStatement.setFloat(3, orderAmount);

            orderStatement.executeUpdate();

            String sqlGetTotal = "select total_amount from monthly_sales";
            sqlGetTotal += " where product_id = ? and report_month = ?";

            getTotalStatement = conn.prepareStatement(sqlGetTotal);
            getTotalStatement.setInt(1, productId);
            getTotalStatement.setInt(2, reportMonth);

            ResultSet rs = getTotalStatement.executeQuery();
            rs.next();
            float totalAmount = rs.getFloat("total_amount");
            rs.close();

            if (totalAmount + orderAmount < 10000) {
                // roll back the transaction to the savepoint:
                conn.rollback(savepoint);

            }

            String sqlUpdateTotal = "update monthly_sales set total_amount = total_amount + ?";
            sqlUpdateTotal += " where product_id = ? and report_month = ?";


            saleStatement = conn.prepareStatement(sqlUpdateTotal);


            saleStatement.setFloat(1, orderAmount);
            saleStatement.setInt(2, productId);
            saleStatement.setInt(3, reportMonth);


            saleStatement.executeUpdate();

            conn.commit();

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    // abort the transaction
                    conn.rollback();

                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
        } finally {
            try {

                // close statements
                if (productStatement != null ) {
                    productStatement.close();
                }

                if (orderStatement != null ) {
                    orderStatement.close();
                }

                if (saleStatement != null ) {
                    saleStatement.close();
                }

                if (getTotalStatement != null ) {
                    getTotalStatement.close();
                }

                conn.setAutoCommit(true);
            } catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        JDBCTransactionSavePointDemo demo = new JDBCTransactionSavePointDemo();

        String newProductName = "iPod";
        float newProductPrice = 399;

        int productId = 1;
        int reportMonth = 7;
        Date date = new Date(System.currentTimeMillis());
        float orderAmount = 580;

        demo.connect();

        demo.saveOrder(newProductName, newProductPrice, productId, date, orderAmount, reportMonth);

        demo.disconnect();
    }

}
