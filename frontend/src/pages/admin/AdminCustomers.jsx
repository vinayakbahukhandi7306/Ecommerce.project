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
        <div>
          <h1>Customer Management</h1>
          <p>View registered customers in your store.</p>
        </div>
      </div>

      {error && (
        <p className="admin-customers-error">{error}</p>
      )}

      {customers.length === 0 ? (
        <div className="empty-customers">
          <h2>No customers found</h2>
          <p>There are currently no registered customers.</p>
        </div>
      ) : (
        <>
          <div className="customers-summary">
            <span>
              {customers.length} registered customer
              {customers.length !== 1 ? "s" : ""}
            </span>
          </div>

          <div className="customers-table-wrapper">
            <table className="customers-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Customer</th>
                  <th>Email</th>
                </tr>
              </thead>

              <tbody>
                {customers.map((customer) => (
                  <tr key={customer.id}>
                    <td className="customer-id">
                      #{customer.id}
                    </td>

                    <td className="customer-name">
                      {customer.firstName}{" "}
                      {customer.lastName}
                    </td>

                    <td>{customer.email}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

export default AdminCustomers;