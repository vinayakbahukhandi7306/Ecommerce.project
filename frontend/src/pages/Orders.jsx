import { useEffect, useState } from "react";
import api from "../services/api";
import "./Orders.css";

function Orders() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const response = await api.get("/api/orders/my");
      setOrders(response.data);
    } catch (error) {
      console.error(error);
      setError("Failed to load orders");
    }
  };

  return (
    <div className="orders-page">
      <div className="orders-container">
        <h1>My Orders</h1>

        {error && (
          <p className="orders-error">
            {error}
          </p>
        )}

        {orders.length === 0 && !error && (
          <div className="empty-orders">
            <h2>No Orders Yet</h2>

            <p>
              You haven't placed any orders yet.
            </p>
          </div>
        )}

        {orders.length > 0 && (
          <div className="orders-list">
            {orders.map((order) => (
              <div
                className="order-card"
                key={order.id}
              >
                <div className="order-header">
                  <h2>Order #{order.id}</h2>

                  <span
                    className={`order-status ${order.status.toLowerCase()}`}
                  >
                    {order.status}
                  </span>
                </div>

                <div className="order-details">
                  <p>
                    <strong>Product:</strong>{" "}
                    {order.productName}
                  </p>

                  <p>
                    <strong>Quantity:</strong>{" "}
                    {order.quantity}
                  </p>

                  <p>
                    <strong>Total:</strong> ₹
                    {order.totalPrice}
                  </p>

                  <p>
                    <strong>Ordered:</strong>{" "}
                    {order.createdAt}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default Orders;