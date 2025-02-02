import React from 'react';

const AuthContext = React.createContext({
  authenticated: false,
  token: null,
  role: null,
});

export default AuthContext;
