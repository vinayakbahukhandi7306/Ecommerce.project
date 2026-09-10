import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Navbar.css";

function Navbar() {
  const { isLoggedIn, isAdmin, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  return (
    <nav>
      <Link to="/">
        <strong>E-Commerce</strong>
      </Link>

      <Link to="/products">Products</Link>

      {isLoggedIn && (
        <>
          <Link to="/cart">Cart</Link>
          <Link to="/orders">Orders</Link>

          {isAdmin && (
            <>
              <Link to="/admin/dashboard">Admin Dashboard</Link>
              <Link to="/admin/products">Admin Products</Link>
            </>
          )}

          <button onClick={handleLogout}>Logout</button>
        </>
      )}

      {!isLoggedIn && (
        <>
          <Link to="/login">Login</Link>

          <Link to="/register">Register</Link>
        </>
      )}
    </nav>
  );
}

export default Navbar;