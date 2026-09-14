import { createContext, useContext, useState } from "react";
import { jwtDecode } from "jwt-decode";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [token, setToken] = useState(
    localStorage.getItem("token")
  );

  const getRoleFromToken = (jwtToken) => {
    if (!jwtToken) {
      return null;
    }

    try {
      const decodedToken = jwtDecode(jwtToken);

      return (
        decodedToken.role ||
        decodedToken.roles ||
        null
      );
    } catch (error) {
      console.error("Invalid token:", error);
      return null;
    }
  };

  const login = (newToken) => {
    localStorage.setItem("token", newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
  };

  const role = getRoleFromToken(token);

  const isLoggedIn = !!token;
  const isAdmin = role === "ADMIN" || role === "ROLE_ADMIN";

  return (
    <AuthContext.Provider
      value={{
        token,
        role,
        isLoggedIn,
        isAdmin,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}