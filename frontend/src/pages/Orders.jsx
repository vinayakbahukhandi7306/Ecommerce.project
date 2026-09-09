import { useEffect, useState } from "react";
import api from "../services/api";

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
    <div>
      <h1>My Orders</h1>

      {error && <p>{error}</p>}

      {orders.length === 0 && !error && (
        <p>You have no orders.</p>
      )}

      {orders.map((order) => (
        <div key={order.id}>
          <h2>Order #{order.id}</h2>

          <p>Product: {order.productName}</p>

          <p>Quantity: {order.quantity}</p>

          <p>Total: ₹{order.totalPrice}</p>

          <p>Status: {order.status}</p>

          <p>Ordered: {order.createdAt}</p>

          <hr />
        </div>
      ))}
    </div>
  );
}

export default Orders;