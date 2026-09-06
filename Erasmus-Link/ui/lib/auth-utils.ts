"use client"

// Types for user data
export interface User {
  id: number
  username: string
  fullName: string
  email: string
  userType: string
  createdAt: string
  lastLogin: string
}

// Save user to session storage
export const saveUser = (user: User): void => {
  sessionStorage.setItem('user', JSON.stringify(user))
}

// Get user from session storage
export const getUser = (): User | null => {
  if (typeof window === 'undefined') return null
  
  const userStr = sessionStorage.getItem('user')
  if (!userStr) return null
  
  try {
    return JSON.parse(userStr) as User
  } catch (error) {
    console.error('Error parsing user data from session storage', error)
    return null
  }
}

// Check if user is logged in
export const isLoggedIn = (): boolean => {
  return getUser() !== null
}

// Get user type (role)
export const getUserType = (): string | null => {
  const user = getUser()
  return user ? user.userType : null
}

// Logout (clear session storage)
export const logout = (): void => {
  sessionStorage.removeItem('user')
  // You might also want to redirect to login page
  window.location.href = '/login'
}

// Update user in session storage (e.g., after profile update)
export const updateUser = (updatedUser: Partial<User>): void => {
  const currentUser = getUser()
  if (!currentUser) return
  
  const newUser = { ...currentUser, ...updatedUser }
  saveUser(newUser)
}

// Format user type to have first letter uppercase and the rest lowercase
export const formatUserType = (userType: string): string => {
  if (!userType) return '';
  return userType.charAt(0).toUpperCase() + userType.slice(1).toLowerCase();
}
