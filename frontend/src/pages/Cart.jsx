import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import "./Cart.css";

function Cart() {
  const [cart, setCart] = useState(null);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    fetchCart();
  }, []);

  const fetchCart = async () => {
    try {
      const response = await api.get("/api/cart");
      setCart(response.data);
    } catch (error) {
      console.error(error);
      setError("Failed to load cart");
    }
  };

  const handleRemove = async (productId) => {
    try {
      await api.delete(`/api/cart/${productId}`);
      await fetchCart();

      alert("Product removed from cart!");
    } catch (error) {
      console.error(error);

      alert(
        error.response?.data?.message ||
          "Failed to remove product"
      );
    }
  };

  const handleUpdateQuantity = async (productId, quantity) => {
    if (quantity < 1) {
      alert("Quantity must be at least 1");
      return;
    }

    try {
      await api.put(
        `/api/cart/${productId}?quantity=${quantity}`
      );

      await fetchCart();

      alert("Cart updated!");
    } catch (error) {
      console.error(error);

      alert(
        error.response?.data?.message ||
          "Failed to update quantity"
      );
    }
  };

  return (
    <div className="cart-page">
      <h1>My Cart</h1>

      {error && (
        <p className="cart-error">
          {error}
        </p>
      )}

      {cart && cart.items.length === 0 && (
        <div className="empty-cart">
          <h2>Your cart is empty</h2>

          <p>Add some products to your cart to get started.</p>

          <button onClick={() => navigate("/products")}>
            Continue Shopping
          </button>
        </div>
      )}

      {cart && cart.items.length > 0 && (
        <>
          <div className="cart-items">
            {cart.items.map((item) => (
              <div
                className="cart-item"
                key={item.productId}
              >
                <div className="cart-item-info">
                  <h2>{item.productName}</h2>

                  <p>
                    Price: ₹{item.price}
                  </p>
                </div>

                <div className="quantity-controls">
                  <button
                    onClick={() =>
                      handleUpdateQuantity(
                        item.productId,
                        item.quantity - 1
                      )
                    }
                    disabled={item.quantity <= 1}
                  >
                    −
                  </button>

                  <span>{item.quantity}</span>

                  <button
                    onClick={() =>
                      handleUpdateQuantity(
                        item.productId,
                        item.quantity + 1
                      )
                    }
                  >
                    +
                  </button>
                </div>

                <button
                  className="remove-btn"
                  onClick={() =>
                    handleRemove(item.productId)
                  }
                >
                  Remove
                </button>
              </div>
            ))}
          </div>

          <div className="cart-actions">
            <button
              className="continue-btn"
              onClick={() => navigate("/products")}
            >
              Continue Shopping
            </button>

            <button
              className="checkout-btn"
              onClick={() => navigate("/checkout")}
            >
              Proceed to Checkout
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default Cart;