function Register() {
  return (
    <div>
      <h1>Register</h1>

      <form>
        <div>
          <label>First Name</label>
          <input type="text" placeholder="Enter first name" />
        </div>

        <div>
          <label>Last Name</label>
          <input type="text" placeholder="Enter last name" />
        </div>

        <div>
          <label>Email</label>
          <input type="email" placeholder="Enter email" />
        </div>

        <div>
          <label>Password</label>
          <input type="password" placeholder="Enter password" />
        </div>

        <button type="submit">Register</button>
      </form>
    </div>
  );
}

export default Register;