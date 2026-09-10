import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import "./Checkout.css";

function Checkout() {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [orders, setOrders] = useState([]);

  const navigate = useNavigate();

  const handleCheckout = async () => {
    try {
      setLoading(true);
      setMessage("");
      setOrders([]);

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
    <div className="checkout-page">
      <div className="checkout-container">
        <h1>Checkout</h1>

        {!orders.length && (
          <>
            <p className="checkout-description">
              Review your order and place it securely.
            </p>

            <button
              className="place-order-btn"
              onClick={handleCheckout}
              disabled={loading}
            >
              {loading ? "Processing..." : "Place Order"}
            </button>
          </>
        )}

        {message && (
          <p
            className={
              orders.length > 0
                ? "success-message"
                : "checkout-message"
            }
          >
            {message}
          </p>
        )}

        {orders.length > 0 && (
          <div className="order-success">
            <h2>Order Successful!</h2>

            <div className="order-list">
              {orders.map((order) => (
                <div
                  className="checkout-order"
                  key={order.id}
                >
                  <p>
                    <strong>Order ID:</strong> {order.id}
                  </p>

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
                    <strong>Status:</strong>{" "}
                    {order.status}
                  </p>
                </div>
              ))}
            </div>

            <div className="checkout-actions">
              <button
                onClick={() => navigate("/products")}
              >
                Continue Shopping
              </button>

              <button
                onClick={() => navigate("/orders")}
              >
                View My Orders
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Checkout;