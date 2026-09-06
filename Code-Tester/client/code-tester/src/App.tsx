import './App.css'

import Header from './components/Layout/Header/Header'
import Login from './components/Form/Login/Login'
import { Routes, Route } from 'react-router-dom'

function App() {

  return (
    <div id='global-container'>
      <Header />

      <Routes>
        <Route path='/login' element={<Login />} />
      </Routes>
    </div>
  )
}

export default App
