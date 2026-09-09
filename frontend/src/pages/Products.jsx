import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import ProductCard from "../components/ProductCard";

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
    <div>
      <h1>Products</h1>

      <Link to="/cart">
        <button>View Cart</button>
      </Link>

      <hr />

      {error && <p>{error}</p>}

      {products.length === 0 && !error && (
        <p>No products available.</p>
      )}

      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
        />
      ))}
    </div>
  );
}

export default Products;