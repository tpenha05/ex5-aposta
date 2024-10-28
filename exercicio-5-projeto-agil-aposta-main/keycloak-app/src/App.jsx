import { useEffect, useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

import { useKeycloak } from '@react-keycloak/web';
import { Link, Route, Routes } from 'react-router-dom';
import ListarAposta from './time/ListarAposta';

function App() {
  const { keycloak, initialized } = useKeycloak();


  const handleLogin = () => {
    keycloak.login();
  };

  const handleLogout = () => {
    keycloak.logout();
  };
  
  return (
    <div>
    {initialized && keycloak.authenticated ? (
      <div>
        <div>
          <div>
            <Link to='/ListarAposta'>Listar Aposta</Link>
          </div>
          <div>  
            <button onClick={handleLogout}>Logout</button>
          </div>
        </div>
        <h1>Bem-vindo, {keycloak.tokenParsed.preferred_username}!</h1>
        {keycloak.token}

        <Routes>
          <Route path='/ListarAposta' element={<ListarAposta />} />
        </Routes>
      </div>

    ) : (
      <div>
        <h1>Você não está autenticado.</h1>
        <button onClick={handleLogin}>Login</button>
      </div>
    )}
  </div>
  )
}

export default App
