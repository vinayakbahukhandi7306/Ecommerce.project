import { useEffect, useState } from "react";
import api from "../../services/api";
import "./AdminCustomers.css";

function AdminCustomers() {
  const [customers, setCustomers] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchCustomers();
  }, []);

  const fetchCustomers = async () => {
    try {
      const response = await api.get("/api/admin/customers");
      setCustomers(response.data);
    } catch (error) {
      console.error(error);

      setError(
        error.response?.data?.message ||
          "Failed to load customers"
      );
    }
  };

  return (
    <div className="admin-customers">
      <div className="admin-customers-header">
        <h1>Customer Management</h1>
        <p>View registered customers in your store.</p>
      </div>

      {error && (
        <p className="admin-customers-error">{error}</p>
      )}

      {customers.length === 0 ? (
        <p>No customers found.</p>
      ) : (
        <div className="customers-table-wrapper">
          <table className="customers-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
              </tr>
            </thead>

            <tbody>
              {customers.map((customer) => (
                <tr key={customer.id}>
                  <td>{customer.id}</td>
                  <td>
                    {customer.firstName} {customer.lastName}
                  </td>
                  <td>{customer.email}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default AdminCustomers;