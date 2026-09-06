import styles from './Header.module.css'

import { Link } from 'react-router-dom'
interface NavOptions {
  title: string
  path: string
  iconClassName: string
}
const Header = () => {
  const guestOptions: NavOptions[] = [
    { title: 'Register', path: '/register', iconClassName: 'fa-duotone fa-right-to-bracket' },
    { title: 'Login', path: '/login', iconClassName: 'fa-duotone fa-right-to-bracket' },
  ]

  return (
    <header className={styles['header']}>
      <nav className={styles['navbar']}>
        <section className={styles['navbar-brand']}>
          <div className={styles['image-container']}>
            <img className={styles['logo']} id='logo' src='/cyclone.png' alt='cyclone' />
          </div>

          <h1 className={styles['logo-text']}>
            Cyclone Teseter
          </h1>
        </section>

        <section className={styles['navbar-menu']}>
          <ul>
            {guestOptions.map((option) => (
              <li key={option.path}>
                <Link to={option.path}>
                  <p>{option.title}</p>
                  <i className={option.iconClassName}></i>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      </nav>
    </header>
  )
}

export default Header
