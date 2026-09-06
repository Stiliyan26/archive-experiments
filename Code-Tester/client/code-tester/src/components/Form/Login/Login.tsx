import styles from './Login.module.css';
const Login = () => {
  return (
    <div className={styles.container}>
      <form className='form' action="">
        <div className='input-container'>
          <label>Username</label>
          <input type="text" />
        </div>
      </form>
    </div>
  )
}

export default Login
