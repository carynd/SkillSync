import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to={isAuthenticated ? '/dashboard' : '/'} className="navbar-brand">
          SkillSync
        </Link>

        {isAuthenticated && (
          <div className="navbar-menu">
            <Link to="/dashboard" className="nav-link">Dashboard</Link>
            <Link to="/profile" className="nav-link">Profile</Link>
            <Link to="/jobs" className="nav-link">Jobs</Link>
            <Link to="/ai-chat" className="nav-link">AI Chat</Link>
            <div className="navbar-user">
              <span className="user-name">{user?.name}</span>
              <button onClick={handleLogout} className="btn-logout">
                Logout
              </button>
            </div>
          </div>
        )}
      </div>
    </nav>
  )
}

export default Navbar
