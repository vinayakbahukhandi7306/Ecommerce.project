import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";
import "./Register.css";

function Register() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();

    try {
      await api.post("/api/auth/register", {
        firstName,
        lastName,
        email,
        password,
      });

      alert(
        "Registration successful! An OTP has been sent to your email."
      );

      navigate("/verify-otp", {
        state: { email },
      });
    } catch (error) {
      console.error(error);

      alert(
        error.response?.data?.message ||
          error.response?.data ||
          "Registration failed"
      );
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Create Account</h1>

        <p className="auth-subtitle">
          Register to start shopping
        </p>

        <form onSubmit={handleRegister}>
          <div className="form-row">
            <div className="form-group">
              <label>First Name</label>

              <input
                type="text"
                placeholder="First name"
                value={firstName}
                onChange={(e) =>
                  setFirstName(e.target.value)
                }
                required
              />
            </div>

            <div className="form-group">
              <label>Last Name</label>

              <input
                type="text"
                placeholder="Last name"
                value={lastName}
                onChange={(e) =>
                  setLastName(e.target.value)
                }
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Email</label>

            <input
              type="email"
              placeholder="Enter your email"
              value={email}
              onChange={(e) =>
                setEmail(e.target.value)
              }
              required
            />
          </div>

          <div className="form-group">
            <label>Password</label>

            <input
              type="password"
              placeholder="Create a password"
              value={password}
              onChange={(e) =>
                setPassword(e.target.value)
              }
              required
            />
          </div>

          <button
            className="auth-btn"
            type="submit"
          >
            Create Account
          </button>
        </form>

        <p className="auth-footer">
          Already have an account?{" "}
          <Link to="/login">Login</Link>
        </p>
      </div>
    </div>
  );
}

export default Register;