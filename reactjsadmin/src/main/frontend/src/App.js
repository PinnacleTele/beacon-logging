import './App.css';
import {
  BrowserRouter as Router, 
  Routes,
  Route,
} from "react-router-dom";
import Account from './Pages/Account';
import Login from './Pages/Login';
import Groups from './Pages/Groups';


function App() {
  return (
    <div className="App">
      <Router> 
        <Routes>



         <Route path="/account" element={<Account />} />
         <Route path="/" element={<Login/>} />
         <Route path="/groups" element={<Groups/>} />






        </Routes>
      </Router>
    </div>
  );
}

export default App;
