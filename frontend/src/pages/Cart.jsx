import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

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
    } catch (error) {
      console.error(error);

      alert(
        error.response?.data?.message ||
          "Failed to update quantity"
      );
    }
  };

  return (
    <div>
      <h1>My Cart</h1>

      {error && <p>{error}</p>}

      {cart && cart.items.length === 0 && (
        <p>Your cart is empty.</p>
      )}

      {cart &&
        cart.items.map((item) => (
          <div key={item.productId}>
            <h2>{item.productName}</h2>

            <p>Price: ₹{item.price}</p>

            <p>Quantity:</p>

            <button
              onClick={() =>
                handleUpdateQuantity(
                  item.productId,
                  item.quantity - 1
                )
              }
            >
              -
            </button>

            <span> {item.quantity} </span>

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

            <br />

            <button
              onClick={() => handleRemove(item.productId)}
            >
              Remove
            </button>

            <hr />
          </div>
        ))}

      {cart && cart.items.length > 0 && (
        <button onClick={() => navigate("/checkout")}>
          Proceed to Checkout
        </button>
      )}
    </div>
  );
}

export default Cart;