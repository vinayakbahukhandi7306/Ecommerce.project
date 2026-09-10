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
        <p className="admin-error">{error}</p>
      </div>
    );
  }

  if (!dashboard) {
    return (
      <div className="admin-dashboard">
        <p>Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      <div className="admin-dashboard-header">
        <h1>Admin Dashboard</h1>
        <p>Overview of your e-commerce store.</p>
      </div>

      <div className="dashboard-stats">
        <div className="stat-card">
          <h2>Total Products</h2>
          <p>{dashboard.totalProducts}</p>
        </div>

        <div className="stat-card">
          <h2>Total Orders</h2>
          <p>{dashboard.totalOrders}</p>
        </div>

        <div className="stat-card">
          <h2>Total Customers</h2>
          <p>{dashboard.totalCustomers}</p>
        </div>

        <div className="stat-card">
          <h2>Total Revenue</h2>
          <p>₹{dashboard.totalRevenue}</p>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;