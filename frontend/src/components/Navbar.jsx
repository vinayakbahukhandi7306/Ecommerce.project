import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Navbar.css";

function Navbar() {
  const { isLoggedIn, isAdmin, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        E-Commerce
      </Link>

      <div className="navbar-links">
        <Link to="/products">Products</Link>

        {isLoggedIn && (
          <>
            <Link to="/cart">Cart</Link>
            <Link to="/orders">Orders</Link>

            {isAdmin && (
              <div className="admin-nav">
                <span className="admin-label">Admin</span>

                <Link to="/admin/dashboard">
                  Dashboard
                </Link>

                <Link to="/admin/products">
                  Products
                </Link>

                <Link to="/admin/orders">
                  Orders
                </Link>

                <Link to="/admin/customers">
                  Customers
                </Link>
              </div>
            )}

            <button
              className="logout-btn"
              onClick={handleLogout}
            >
              Logout
            </button>
          </>
        )}

        {!isLoggedIn && (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;