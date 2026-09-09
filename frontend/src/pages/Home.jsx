import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Home() {
  const { isLoggedIn, logout } = useAuth();

  const handleLogout = () => {
    logout();
    alert("Logged out successfully!");
  };

  return (
    <div>
      <h1>E-Commerce App</h1>

      <p>Welcome to our store!</p>

      {!isLoggedIn && (
        <div>
          <Link to="/login">
            <button>Login</button>
          </Link>
        </div>
      )}

      {isLoggedIn && (
        <div>
          <button onClick={handleLogout}>
            Logout
          </button>
        </div>
      )}

      <div>
        <Link to="/products">
          <button>View Products</button>
        </Link>
      </div>

      <div>
        <Link to="/cart">
          <button>View Cart</button>
        </Link>
      </div>

      <div>
        <Link to="/orders">
          <button>My Orders</button>
        </Link>
      </div>
    </div>
  );
}

export default Home;