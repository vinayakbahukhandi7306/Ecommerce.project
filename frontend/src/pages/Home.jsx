import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Home.css";

function Home() {
  const { isLoggedIn, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="home">
      <section className="hero">
        <h1>Welcome to E-Commerce</h1>

        <p>
          Discover great products and shop with ease.
        </p>

        <Link to="/products">
          <button className="primary-btn">
            Start Shopping
          </button>
        </Link>
      </section>

      <section className="home-actions">
        {!isLoggedIn && (
          <>
            <Link to="/login">
              <button>Login</button>
            </Link>

            <Link to="/register">
              <button>Register</button>
            </Link>
          </>
        )}

        {isLoggedIn && (
          <>
            <Link to="/cart">
              <button>View Cart</button>
            </Link>

            <Link to="/orders">
              <button>My Orders</button>
            </Link>

            <button onClick={handleLogout}>
              Logout
            </button>
          </>
        )}
      </section>
    </div>
  );
}

export default Home;