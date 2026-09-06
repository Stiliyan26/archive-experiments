"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { AlertCircle, Check, X } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { saveUser } from "../../lib/auth-utils"

interface FormErrors {
  username?: string;
  password?: string;
}

interface ValidationState {
  username: {
    valid: boolean;
    message: string;
    criteria: {
      minLength: boolean;
      format: boolean;
    };
  };
  password: {
    valid: boolean;
    message: string;
    criteria: {
      minLength: boolean;
    };
  };
}

export default function LoginPage() {
  const router = useRouter()
  const [formData, setFormData] = useState({
    username: "",
    password: ""
  })
  
  const [errors, setErrors] = useState<FormErrors>({})
  const [formError, setFormError] = useState("")
  const [loading, setLoading] = useState(false)
  const [showValidation, setShowValidation] = useState<boolean>(false)
  const [attemptCount, setAttemptCount] = useState<number>(0)
  
  // Initialize validation state
  const [validation, setValidation] = useState<ValidationState>({
    username: {
      valid: false,
      message: "",
      criteria: {
        minLength: false,
        format: false
      }
    },
    password: {
      valid: false,
      message: "",
      criteria: {
        minLength: false
      }
    }
  })

  // Update validation state when form data changes
  useEffect(() => {
    validateUsername(formData.username)
    validatePassword(formData.password)
  }, [formData])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target
    setFormData((prev) => ({ ...prev, [id]: value }))
    
    // Clear specific field error when user types
    if (errors[id as keyof FormErrors]) {
      setErrors(prev => ({...prev, [id]: undefined}))
    }
    
    // Clear form error when user starts typing after a failed attempt
    if (formError) {
      setFormError("")
    }
  }

  const validateUsername = (username: string) => {
    const hasMinLength = username.length >= 3
    const hasValidFormat = /^[a-zA-Z0-9_]+$/.test(username)
    
    const isValid = hasMinLength && hasValidFormat
    
    setValidation(prev => ({
      ...prev,
      username: {
        valid: isValid,
        message: !isValid ? "Username must be at least 3 characters with only letters, numbers, and underscores" : "",
        criteria: {
          minLength: hasMinLength,
          format: hasValidFormat
        }
      }
    }))
    
    return isValid
  }

  const validatePassword = (password: string) => {
    const hasMinLength = password.length >= 8
    
    const isValid = hasMinLength
    
    setValidation(prev => ({
      ...prev,
      password: {
        valid: isValid,
        message: !isValid ? "Password must be at least 8 characters" : "",
        criteria: {
          minLength: hasMinLength
        }
      }
    }))
    
    return isValid
  }

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}
    let isValid = true
    
    // Validate username
    if (!validation.username.valid) {
      newErrors.username = validation.username.message
      isValid = false
    }
    
    // Validate password
    if (!validation.password.valid) {
      newErrors.password = validation.password.message
      isValid = false
    }
    
    setErrors(newErrors)
    setShowValidation(true)
    return isValid
  }

  // Helper function to render validation status icon
  const renderCriteriaIcon = (isValid: boolean) => {
    return isValid 
      ? <Check className="h-4 w-4 text-green-500" /> 
      : <X className="h-4 w-4 text-red-500" />
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setFormError("")
    
    // Validate form
    if (!validateForm()) {
      return
    }
    
    setLoading(true)

    try {
      // Call the backend API to login
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({
          username: formData.username,
          password: formData.password
        }),
      })

      if (!response.ok) {
        // Increment attempt count
        setAttemptCount(prev => prev + 1)
        
        const errorData = await response.json()
        throw new Error(errorData.message || 'Invalid username or password')
      }

      // Get the user data from the response
      const userData = await response.json()
      
      // Save user data to session storage using our utility function
      saveUser(userData)
      
      // Reset attempt count on successful login
      setAttemptCount(0)
      
      // Redirect to dashboard
      router.push('/')
    } catch (err) {
      let errorMessage = err instanceof Error ? err.message : 'An error occurred during login'
      
      // Provide more specific guidance after multiple failed attempts
      if (attemptCount >= 2) {
        errorMessage += '. If you continue to have trouble, you may want to reset your password.'
      }
      
      setFormError(errorMessage)
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1 flex items-center justify-center py-12">
        <Card className="w-full max-w-md">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold">Login</CardTitle>
            <CardDescription>Enter your credentials to access your account</CardDescription>
          </CardHeader>
          <form onSubmit={handleSubmit}>
            <CardContent className="space-y-4">
              {formError && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{formError}</AlertDescription>
                </Alert>
              )}
              
              {/* Username Field */}
              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input
                  id="username"
                  placeholder="JohnDoe"
                  value={formData.username}
                  onChange={handleChange}
                  className={errors.username ? "border-red-500" : ""}
                  required
                  onFocus={() => setShowValidation(true)}
                />
                {errors.username && <p className="text-red-500 text-xs mt-1">{errors.username}</p>}
                
                {showValidation && formData.username && (
                  <div className="text-xs space-y-1 mt-1">
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.username.criteria.minLength)}
                      <span className="ml-2">At least 3 characters</span>
                    </div>
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.username.criteria.format)}
                      <span className="ml-2">Only letters, numbers, and underscores</span>
                    </div>
                  </div>
                )}
              </div>
              
              {/* Password Field */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label htmlFor="password">Password</Label>
                  <Link href="/forgot-password" className="text-sm text-muted-foreground hover:underline">
                    Forgot password?
                  </Link>
                </div>
                <Input
                  id="password"
                  type="password"
                  value={formData.password}
                  onChange={handleChange}
                  className={errors.password ? "border-red-500" : ""}
                  required
                  onFocus={() => setShowValidation(true)}
                />
                {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
                
                {showValidation && formData.password && (
                  <div className="text-xs space-y-1 mt-1">
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.password.criteria.minLength)}
                      <span className="ml-2">At least 8 characters</span>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
            <CardFooter className="flex flex-col space-y-4">
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? "Logging in..." : "Login"}
              </Button>
              <div className="text-center text-sm text-muted-foreground">
                Don&apos;t have an account?{" "}
                <Link href="/register" className="font-medium text-primary hover:underline">
                  Register
                </Link>
              </div>
            </CardFooter>
          </form>
        </Card>
      </main>
      <Footer />
    </div>
  )
}