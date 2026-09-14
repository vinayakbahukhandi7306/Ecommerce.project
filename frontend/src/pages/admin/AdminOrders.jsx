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

      await fetchOrders();
    } catch (error) {
      console.error(error);
      setError(
        error.response?.data?.message ||
          "Failed to update order status"
      );
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
        <p>No orders found.</p>
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
              </tr>
            </thead>

            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>#{order.id}</td>

                  <td>{order.userEmail}</td>

                  <td>{order.productName}</td>

                  <td>{order.quantity}</td>

                  <td>₹{order.totalPrice}</td>

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