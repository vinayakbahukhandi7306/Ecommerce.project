import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import ProductCard from "../components/ProductCard";
import "./Products.css";

function Products() {
  const [products, setProducts] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await api.get("/api/products");
      setProducts(response.data);
    } catch (error) {
      console.error(error);
      setError("Failed to load products");
    }
  };

  return (
    <div className="products-page">
      <div className="products-header">
        <div>
          <h1>Our Products</h1>
          <p>Browse our collection and find what you need.</p>
        </div>

        <Link to="/cart">
          <button className="cart-btn">View Cart</button>
        </Link>
      </div>

      {error && <p className="error-message">{error}</p>}

      {products.length === 0 && !error && (
        <p className="empty-message">No products available.</p>
      )}

      <div className="products-grid">
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
          />
        ))}
      </div>
    </div>
  );
}

export default Products;