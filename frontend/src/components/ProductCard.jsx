import api from "../services/api";

function ProductCard({ product }) {

  const handleAddToCart = async () => {
    try {
      await api.post(`/api/cart?productId=${product.id}&quantity=1`);

      alert("Product added to cart!");
    } catch (error) {
      console.error(error);

      alert(
        error.response?.data?.message || "Failed to add product to cart"
      );
    }
  };

  return (
    <div>
      <h2>{product.name}</h2>

      <p>Price: ₹{product.price}</p>

      <p>Stock: {product.stock}</p>

      <button onClick={handleAddToCart}>
        Add to Cart
      </button>
    </div>
  );
}

export default ProductCard;