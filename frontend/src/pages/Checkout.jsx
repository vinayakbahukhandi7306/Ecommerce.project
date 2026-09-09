import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Checkout() {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [orders, setOrders] = useState([]);

  const navigate = useNavigate();

  const handleCheckout = async () => {
    try {
      setLoading(true);
      setMessage("");

      const response = await api.post("/api/orders/checkout");

      setMessage(response.data.message);
      setOrders(response.data.orders);

    } catch (error) {
      console.error(error);

      setMessage(
        error.response?.data?.message ||
          "Checkout failed"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Checkout</h1>

      <button
        onClick={handleCheckout}
        disabled={loading}
      >
        {loading ? "Processing..." : "Place Order"}
      </button>

      {message && <p>{message}</p>}

      {orders.length > 0 && (
        <div>
          <h2>Order Successful!</h2>

          {orders.map((order) => (
            <div key={order.id}>
              <p>Order ID: {order.id}</p>
              <p>Product: {order.productName}</p>
              <p>Quantity: {order.quantity}</p>
              <p>Total: ₹{order.totalPrice}</p>
              <p>Status: {order.status}</p>
            </div>
          ))}

          <button onClick={() => navigate("/products")}>
            Continue Shopping
          </button>
        </div>
      )}
    </div>
  );
}

export default Checkout;