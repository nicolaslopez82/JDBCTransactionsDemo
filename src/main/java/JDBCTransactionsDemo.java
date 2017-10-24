import java.sql.*;

/**
 *
 * Disabling Auto Commit mode
 * By default, a new connection is in auto-commit mode. This means each SQL statement is treated as a transaction and is automatically committed right after it is executed. So we have to disable the auto commit mode to enable two or more statements to be grouped into a transaction:
 * connection.setAutoCommit(false);
 *
 * Committing the transaction
 * After the auto commit mode is disabled, all subsequent SQL statements are included in the current transaction, and they are committed as a single unit until we call the method commit():
 *
 * connection.commit();
 * So a transaction begins right after the auto commit is disabled and ends right after the connection is committed. Remember to execute SQL statements between these calls to ensure they are in the same transaction.

 * Rolling back the transaction
 * If any statement failed to execute, a SQLException is thrown, and in the catch block, we invoke the method rollback() to abort the transaction:
 *
 * connection.rollback();
 * Any changes made by the successful statements are discarded and the database is rolled back to the previous state before the transaction.
 *
 * Enabling Auto Commit mode
 * Finally, we enable the auto commit mode to get the connection back to the default state:
 * connection.setAutoCommit(true);
 * In the default state (auto commit is enabled), each SQL is treated as a transaction and we don’t need to call the commit() method manually.
 *
 *
 *
 * JDBC Transaction Demo Program
 *
 * A JDBC Transactions Example:
 *
 * Suppose that we are working on a database called sales with the following tables (see tables_structures.png in resources directory):
 *
 * When a new order is saved (a new row inserted into the table orders), the monthly sales also must be updated
 * (a corresponding row gets updated in the table monthly_sales). So these two statements (save order and update sales)
 * should be grouped into a transaction.
 *
 * The following method shows how to execute these two statements in a transaction with JDBC:
 *
 *
 * Created by NicolasLopez on 24/10/2017.
 */
public class JDBCTransactionsDemo {

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

    public void saveOrder(int productId, Date orderDate, float amount, int reportMonth) {
        PreparedStatement orderStatement = null;
        PreparedStatement saleStatement = null;

        try {

            conn.setAutoCommit(false);

            String sqlSaveOrder = "insert into orders (product_id, order_date, amount)";
            sqlSaveOrder += " values (?, ?, ?)";

            String sqlUpdateTotal = "update monthly_sales set total_amount = total_amount + ?";
            sqlUpdateTotal += " where product_id = ? and report_month = ?";

            orderStatement = conn.prepareStatement(sqlSaveOrder);
            saleStatement = conn.prepareStatement(sqlUpdateTotal);

            orderStatement.setInt(1, productId);
            orderStatement.setDate(2, orderDate);
            orderStatement.setFloat(3, amount);

            saleStatement.setFloat(1, amount);
            saleStatement.setInt(2, productId);
            saleStatement.setInt(3, reportMonth);

            orderStatement.executeUpdate();
            saleStatement.executeUpdate();

            conn.commit();

        } catch (SQLException ex) {
            if (conn != null) {
                try {

                    conn.rollback();

                    System.out.println("Rolled back.");
                } catch (SQLException exrb) {
                    exrb.printStackTrace();
                }
            }
        } finally {
            try {
                if (orderStatement != null ) {
                    orderStatement.close();
                }

                if (saleStatement != null ) {
                    saleStatement.close();
                }

                conn.setAutoCommit(true);
            } catch (SQLException excs) {
                excs.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        JDBCTransactionsDemo demo = new JDBCTransactionsDemo();

        int productId = 1;
        int reportMonth = 7;
        Date date = new Date(System.currentTimeMillis());
        float amount = 580;

        demo.connect();

        demo.saveOrder(productId, date, amount, reportMonth);

        demo.disconnect();
    }
}
