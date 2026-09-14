import { useEffect, useState } from "react";
import api from "../../services/api";
import "./AdminDashboard.css";

function AdminDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const response = await api.get("/api/admin/dashboard");
      setDashboard(response.data);
    } catch (error) {
      console.error(error);
      setError(
        error.response?.data?.message ||
          "Failed to load admin dashboard"
      );
    }
  };

  if (error) {
    return (
      <div className="admin-dashboard">
        <div className="admin-dashboard-error">
          <h2>Unable to load dashboard</h2>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (!dashboard) {
    return (
      <div className="admin-dashboard">
        <div className="admin-dashboard-loading">
          Loading dashboard...
        </div>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      <div className="admin-dashboard-header">
        <div>
          <h1>Admin Dashboard</h1>
          <p>Overview of your e-commerce store.</p>
        </div>
      </div>

      <div className="dashboard-stats">
        <div className="stat-card">
          <span className="stat-label">Products</span>
          <p className="stat-value">{dashboard.totalProducts}</p>
          <span className="stat-description">
            Products in store
          </span>
        </div>

        <div className="stat-card">
          <span className="stat-label">Orders</span>
          <p className="stat-value">{dashboard.totalOrders}</p>
          <span className="stat-description">
            Orders placed
          </span>
        </div>

        <div className="stat-card">
          <span className="stat-label">Customers</span>
          <p className="stat-value">
            {dashboard.totalCustomers}
          </p>
          <span className="stat-description">
            Registered customers
          </span>
        </div>

        <div className="stat-card">
          <span className="stat-label">Revenue</span>
          <p className="stat-value">
            ₹{dashboard.totalRevenue}
          </p>
          <span className="stat-description">
            Total non-cancelled revenue
          </span>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;