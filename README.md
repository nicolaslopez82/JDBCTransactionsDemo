# JDBCTransactionsDemo
This Java JDBCTransactionsDemo tutorial intends to help you understand how database transactions work with JDBC.

Why Transactions?
In database systems, transactions are designed to preserve data integrity by grouping multiple statements to be executed as a single unit. In a transaction, either all of the statements are executed, or none of the statements is executed. If any statement failed to execute, the whole transaction is aborted and the database is rolled back to the previous state. This assures the data is kept consistence in the events of network problems, software errors, etc.
Let’s see an example.
Imagine in a sales application, the manager saves a new order and also updates total sales to date in the current month. The order details and the total sales should be updated at the same time, otherwise the data will be inconsistence. Here, the application should group the save order details statement and update total sales statement in a transaction. Both these statements must be executed. If either one statement failed to execute, all changes are discarded.
Transactions also provide protection against conflicts that might arise when multiple users access the same data at the same time, by using locking mechanisms to block access by others to the data that is being accessed by the current transaction.

This source code tutorial includes the .sql to create database requerided in MySql and the driven connector provided by maven dependency.
