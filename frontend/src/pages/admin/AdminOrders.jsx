import { useEffect, useState } from "react";
import api from "../../services/api";
import "./AdminOrders.css";

function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const response = await api.get("/api/orders/admin/all");
      setOrders(response.data);
    } catch (error) {
      console.error(error);
      setError(
        error.response?.data?.message ||
          "Failed to load orders"
      );
    }
  };

  const handleStatusChange = async (orderId, status) => {
    try {
      await api.put(
        `/api/orders/admin/${orderId}/status`,
        { status }
      );

      setError("");
      await fetchOrders();
    } catch (error) {
      console.error(error);
      setError(
        error.response?.data?.message ||
          "Failed to update order status"
      );
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case "PLACED":
        return "status-badge status-placed";
      case "SHIPPED":
        return "status-badge status-shipped";
      case "DELIVERED":
        return "status-badge status-delivered";
      case "CANCELLED":
        return "status-badge status-cancelled";
      default:
        return "status-badge";
    }
  };

  return (
    <div className="admin-orders">
      <div className="admin-orders-header">
        <h1>Order Management</h1>
        <p>View and manage customer orders.</p>
      </div>

      {error && (
        <p className="admin-orders-error">{error}</p>
      )}

      {orders.length === 0 ? (
        <div className="empty-orders">
          <h2>No orders found</h2>
          <p>There are currently no customer orders.</p>
        </div>
      ) : (
        <div className="orders-table-wrapper">
          <table className="orders-table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Customer</th>
                <th>Product</th>
                <th>Quantity</th>
                <th>Total</th>
                <th>Status</th>
                <th>Update</th>
              </tr>
            </thead>

            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td className="order-id">
                    #{order.id}
                  </td>

                  <td>{order.userEmail}</td>

                  <td className="order-product">
                    {order.productName}
                  </td>

                  <td>{order.quantity}</td>

                  <td className="order-total">
                    ₹{order.totalPrice}
                  </td>

                  <td>
                    <span className={getStatusClass(order.status)}>
                      {order.status}
                    </span>
                  </td>

                  <td>
                    <select
                      value={order.status}
                      onChange={(e) =>
                        handleStatusChange(
                          order.id,
                          e.target.value
                        )
                      }
                    >
                      <option value="PLACED">PLACED</option>
                      <option value="SHIPPED">SHIPPED</option>
                      <option value="DELIVERED">DELIVERED</option>
                      <option value="CANCELLED">CANCELLED</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default AdminOrders;