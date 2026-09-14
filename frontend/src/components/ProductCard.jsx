import api from "../services/api";
import "./ProductCard.css";

function ProductCard({ product }) {
  const handleAddToCart = async () => {
    try {
      await api.post(
        `/api/cart?productId=${product.id}&quantity=1`
      );
    } catch (error) {
      console.error(error);

      alert(
        error.response?.data?.message ||
          "Failed to add product to cart"
      );
    }
  };

  return (
    <div className="product-card">
      <div className="product-info">
        <h2>{product.name}</h2>

        <p className="product-price">
          ₹{product.price}
        </p>

        <p
          className={
            product.stock > 0
              ? "product-stock in-stock"
              : "product-stock out-of-stock"
          }
        >
          {product.stock > 0
            ? "In Stock"
            : "Out of Stock"}
        </p>
      </div>

      <button
        className="add-cart-btn"
        onClick={handleAddToCart}
        disabled={product.stock <= 0}
      >
        {product.stock > 0
          ? "Add to Cart"
          : "Out of Stock"}
      </button>
    </div>
  );
}

export default ProductCard;