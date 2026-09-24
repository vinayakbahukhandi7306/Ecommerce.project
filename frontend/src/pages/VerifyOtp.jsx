import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../services/api";
import "./VerifyOtp.css";

function VerifyOtp() {
  const location = useLocation();
  const navigate = useNavigate();

  const email = location.state?.email;

  const [otp, setOtp] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [resendCooldown, setResendCooldown] = useState(0);

  useEffect(() => {
    if (resendCooldown <= 0) {
      return;
    }

    const timer = setInterval(() => {
      setResendCooldown((previous) => previous - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [resendCooldown]);

  const handleVerify = async (e) => {
    e.preventDefault();

    setMessage("");
    setError("");

    if (otp.length !== 6) {
      setError("OTP must be exactly 6 digits");
      return;
    }

    try {
      setLoading(true);

      await api.post("/api/auth/verify-otp", {
        email,
        otp,
      });

      setMessage("Email verified successfully!");

      setTimeout(() => {
        navigate("/login");
      }, 1000);

    } catch (error) {
      console.error(error);

      setError(
        error.response?.data?.message ||
          error.response?.data ||
          "Invalid or expired OTP"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (resendCooldown > 0) {
      return;
    }

    setMessage("");
    setError("");

    try {
      await api.post(
        `/api/auth/resend-otp?email=${encodeURIComponent(email)}`
      );

      setMessage("A new OTP has been sent to your email.");

      setResendCooldown(60);

    } catch (error) {
      console.error(error);

      setError(
        error.response?.data?.message ||
          error.response?.data ||
          "Unable to resend OTP"
      );
    }
  };

  if (!email) {
    return (
      <div className="auth-page">
        <div className="auth-card">
          <h1>Invalid Verification</h1>

          <p className="auth-subtitle">
            No email was provided for verification.
          </p>

          <Link to="/register">
            Go back to registration
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Verify Your Email</h1>

        <p className="auth-subtitle">
          Enter the 6-digit OTP sent to
        </p>

        <p className="verify-email">
          {email}
        </p>

        <form onSubmit={handleVerify}>
          <div className="form-group">
            <label>OTP</label>

            <input
              type="text"
              placeholder="Enter 6-digit OTP"
              value={otp}
              onChange={(e) =>
                setOtp(
                  e.target.value
                    .replace(/\D/g, "")
                    .slice(0, 6)
                )
              }
              maxLength="6"
              inputMode="numeric"
              required
            />
          </div>

          {error && (
            <p className="auth-error">
              {error}
            </p>
          )}

          {message && (
            <p className="auth-success">
              {message}
            </p>
          )}

          <button
            className="auth-btn"
            type="submit"
            disabled={loading}
          >
            {loading ? "Verifying..." : "Verify Email"}
          </button>
        </form>

        <button
          className="resend-btn"
          type="button"
          onClick={handleResend}
          disabled={resendCooldown > 0}
        >
          {resendCooldown > 0
            ? `Resend OTP in ${resendCooldown}s`
            : "Resend OTP"}
        </button>

        <p className="auth-footer">
          <Link to="/login">
            Back to Login
          </Link>
        </p>
      </div>
    </div>
  );
}

export default VerifyOtp;