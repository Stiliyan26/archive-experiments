"use client"

import type React from "react"

import { useState, useEffect } from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { AlertCircle, CheckCircle2, X, Check } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useRouter } from "next/navigation"

interface FormErrors {
  fullName?: string;
  username?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
  userType?: string;
}

interface ValidationState {
  fullName: {
    valid: boolean;
    message: string;
    criteria: {
      minLength: boolean;
      format: boolean;
    };
  };
  username: {
    valid: boolean;
    message: string;
    criteria: {
      minLength: boolean;
      format: boolean;
      noSpaces: boolean;
    };
  };
  email: {
    valid: boolean;
    message: string;
    criteria: {
      format: boolean;
    };
  };
  password: {
    valid: boolean;
    message: string;
    criteria: {
      minLength: boolean;
      hasUpperCase: boolean;
      hasLowerCase: boolean;
      hasNumber: boolean;
      hasSpecialChar: boolean;
    };
  };
  userType: {
    valid: boolean;
    message: string;
  };
}

export default function RegisterPage() {
  const router = useRouter()

  const [formData, setFormData] = useState({
    fullName: "",
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    userType: "",
  })

  const [errors, setErrors] = useState<FormErrors>({})
  const [formError, setFormError] = useState("")
  const [loading, setLoading] = useState(false)
  const [passwordStrength, setPasswordStrength] = useState<string>("weak")
  const [showValidation, setShowValidation] = useState<boolean>(false)
  
  // Initialize validation state
  const [validation, setValidation] = useState<ValidationState>({
    fullName: {
      valid: false,
      message: "",
      criteria: {
        minLength: false,
        format: false
      }
    },
    username: {
      valid: false,
      message: "",
      criteria: {
        minLength: false,
        format: false,
        noSpaces: false
      }
    },
    email: {
      valid: false,
      message: "",
      criteria: {
        format: false
      }
    },
    password: {
      valid: false,
      message: "",
      criteria: {
        minLength: false,
        hasUpperCase: false,
        hasLowerCase: false,
        hasNumber: false,
        hasSpecialChar: false
      }
    },
    userType: {
      valid: false,
      message: ""
    }
  })

  // Update validation state when form data changes
  useEffect(() => {
    validateFullName(formData.fullName)
    validateUsername(formData.username)
    validateEmail(formData.email)
    validatePassword(formData.password)
    validateUserType(formData.userType)
  }, [formData])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target
    setFormData((prev) => ({ ...prev, [id]: value }))
    
    // Clear specific field error when user types
    if (errors[id as keyof FormErrors]) {
      setErrors(prev => ({...prev, [id]: undefined}))
    }
  }

  const handleUserTypeChange = (value: string) => {
    setFormData((prev) => ({ ...prev, userType: value }))
    if (errors.userType) {
      setErrors(prev => ({...prev, userType: undefined}))
    }
  }

  const validateFullName = (fullName: string) => {
    const hasValidLength = fullName.trim().length >= 2
    const hasValidFormat = /^[A-Za-zА-Яа-я\s'-]+$/.test(fullName) // Allow letters, spaces, hyphens, apostrophes
    
    const isValid = hasValidLength && hasValidFormat
    
    setValidation(prev => ({
      ...prev,
      fullName: {
        valid: isValid,
        message: !isValid ? "Full name must be at least 2 characters and contain only letters, spaces, hyphens, and apostrophes" : "",
        criteria: {
          minLength: hasValidLength,
          format: hasValidFormat
        }
      }
    }))
    
    return isValid
  }

  const validateUsername = (username: string) => {
    const hasMinLength = username.length >= 4
    const hasValidFormat = /^[a-zA-Z0-9_]+$/.test(username)
    const hasNoSpaces = !username.includes(' ')
    
    const isValid = hasMinLength && hasValidFormat && hasNoSpaces
    
    setValidation(prev => ({
      ...prev,
      username: {
        valid: isValid,
        message: !isValid ? "Username must be at least 4 characters with only letters, numbers, and underscores" : "",
        criteria: {
          minLength: hasMinLength,
          format: hasValidFormat,
          noSpaces: hasNoSpaces
        }
      }
    }))
    
    return isValid
  }

  const validateEmail = (email: string) => {
    const hasValidFormat = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
    
    const isValid = hasValidFormat
    
    setValidation(prev => ({
      ...prev,
      email: {
        valid: isValid,
        message: !isValid ? "Please enter a valid email address" : "",
        criteria: {
          format: hasValidFormat
        }
      }
    }))
    
    return isValid
  }

  const validatePassword = (password: string) => {
    const hasMinLength = password.length >= 8
    const hasUpperCase = /[A-Z]/.test(password)
    const hasLowerCase = /[a-z]/.test(password)
    const hasNumber = /[0-9]/.test(password)
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password)
    
    const isValid = hasMinLength && hasUpperCase && hasLowerCase && hasNumber
    
    // Check password strength
    const strengthScore = [hasMinLength, hasUpperCase, hasLowerCase, hasNumber, hasSpecialChar]
      .filter(Boolean).length
    
    if (strengthScore <= 2) setPasswordStrength("weak")
    else if (strengthScore <= 4) setPasswordStrength("medium")
    else setPasswordStrength("strong")
    
    setValidation(prev => ({
      ...prev,
      password: {
        valid: isValid,
        message: !isValid ? "Password doesn't meet the required criteria" : "",
        criteria: {
          minLength: hasMinLength,
          hasUpperCase: hasUpperCase,
          hasLowerCase: hasLowerCase,
          hasNumber: hasNumber,
          hasSpecialChar: hasSpecialChar
        }
      }
    }))
    
    return isValid
  }

  const validateUserType = (userType: string) => {
    const isValid = !!userType
    
    setValidation(prev => ({
      ...prev,
      userType: {
        valid: isValid,
        message: !isValid ? "Please select a user type" : ""
      }
    }))
    
    return isValid
  }

  const validateConfirmPassword = (password: string, confirmPassword: string) => {
    return password === confirmPassword
  }

  const getPasswordStrengthColor = () => {
    switch (passwordStrength) {
      case "weak": return "text-red-500"
      case "medium": return "text-yellow-500"
      case "strong": return "text-green-500"
      default: return ""
    }
  }

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}
    let isValid = true
    
    // Validate all fields
    if (!validation.fullName.valid) {
      newErrors.fullName = validation.fullName.message
      isValid = false
    }
    
    if (!validation.username.valid) {
      newErrors.username = validation.username.message
      isValid = false
    }
    
    if (!validation.email.valid) {
      newErrors.email = validation.email.message
      isValid = false
    }
    
    if (!validation.password.valid) {
      newErrors.password = validation.password.message
      isValid = false
    }
    
    if (!validation.userType.valid) {
      newErrors.userType = validation.userType.message
      isValid = false
    }
    
    // Validate confirm password
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = "Please confirm your password"
      isValid = false
    } else if (!validateConfirmPassword(formData.password, formData.confirmPassword)) {
      newErrors.confirmPassword = "Passwords do not match"
      isValid = false
    }
    
    setErrors(newErrors)
    setShowValidation(true)
    return isValid
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
      // Call the backend API to register the user
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        
        body: JSON.stringify({
          fullName: formData.fullName,
          username: formData.username,
          email: formData.email,
          password: formData.password,
          userType: formData.userType
        }),
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Registration failed')
      }

      // Successful registration, redirect to login
      router.push('/login')
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'An error occurred during registration')
      setLoading(false)
    }
  }

  // Helper function to render validation status icon
  const renderCriteriaIcon = (isValid: boolean) => {
    return isValid 
      ? <Check className="h-4 w-4 text-green-500" /> 
      : <X className="h-4 w-4 text-red-500" />
  }

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1 flex items-center justify-center py-12">
        <Card className="w-full max-w-md">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold">Create an account</CardTitle>
            <CardDescription>Enter your information to create an account</CardDescription>
          </CardHeader>
          <form onSubmit={handleSubmit}>
            <CardContent className="space-y-4">
              {formError && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{formError}</AlertDescription>
                </Alert>
              )}
              
              {/* Full Name Field */}
              <div className="space-y-2">
                <Label htmlFor="fullName">Full Name</Label>
                <Input 
                  id="fullName" 
                  placeholder="John Doe" 
                  value={formData.fullName} 
                  onChange={handleChange} 
                  className={errors.fullName ? "border-red-500" : ""}
                  required 
                  onFocus={() => setShowValidation(true)}
                />
                {errors.fullName && <p className="text-red-500 text-xs mt-1">{errors.fullName}</p>}
                
                {showValidation && formData.fullName && (
                  <div className="text-xs space-y-1 mt-1">
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.fullName.criteria.minLength)}
                      <span className="ml-2">At least 2 characters</span>
                    </div>
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.fullName.criteria.format)}
                      <span className="ml-2">Only letters, spaces, hyphens and apostrophes</span>
                    </div>
                  </div>
                )}
              </div>
              
              {/* Username Field */}
              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input 
                  id="username" 
                  placeholder="JohnDoeTheBest" 
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
                      <span className="ml-2">At least 4 characters</span>
                    </div>
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.username.criteria.format)}
                      <span className="ml-2">Only letters, numbers, and underscores</span>
                    </div>
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.username.criteria.noSpaces)}
                      <span className="ml-2">No spaces</span>
                    </div>
                  </div>
                )}
              </div>
              
              {/* Email Field */}
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="m.example@mail.com"
                  value={formData.email}
                  onChange={handleChange}
                  className={errors.email ? "border-red-500" : ""}
                  required
                  onFocus={() => setShowValidation(true)}
                />
                {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                
                {showValidation && formData.email && (
                  <div className="text-xs space-y-1 mt-1">
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.email.criteria.format)}
                      <span className="ml-2">Valid email format (example@domain.com)</span>
                    </div>
                  </div>
                )}
              </div>
              
              {/* User Type Selection */}
              <div className="space-y-2">
                <Label htmlFor="userType">I am a(n)</Label>
                <Select 
                  value={formData.userType} 
                  onValueChange={handleUserTypeChange} 
                  required
                >
                  <SelectTrigger className={errors.userType ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select user type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="PARTICIPANT">Erasmus Participant</SelectItem>
                    <SelectItem value="ORGANIZATION">Organization / Institution</SelectItem>
                    <SelectItem value="ADMIN">Administrator</SelectItem>
                  </SelectContent>
                </Select>
                {errors.userType && <p className="text-red-500 text-xs mt-1">{errors.userType}</p>}
              </div>
              
              {/* Password Field */}
              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
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
                
                {formData.password && (
                  <div className="flex items-center mt-1">
                    <span className="text-xs mr-2">Password strength:</span>
                    <span className={`text-xs font-semibold ${getPasswordStrengthColor()}`}>
                      {passwordStrength.charAt(0).toUpperCase() + passwordStrength.slice(1)}
                    </span>
                  </div>
                )}
                
                {showValidation && formData.password && (
                  <div className="text-xs space-y-1 mt-1">
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.password.criteria.minLength)}
                      <span className="ml-2">At least 8 characters</span>
                    </div>
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.password.criteria.hasUpperCase)}
                      <span className="ml-2">At least one uppercase letter</span>
                    </div>
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.password.criteria.hasLowerCase)}
                      <span className="ml-2">At least one lowercase letter</span>
                    </div>
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.password.criteria.hasNumber)}
                      <span className="ml-2">At least one number</span>
                    </div>
                    <div className="flex items-center">
                      {renderCriteriaIcon(validation.password.criteria.hasSpecialChar)}
                      <span className="ml-2">At least one special character (recommended)</span>
                    </div>
                  </div>
                )}
              </div>
              
              {/* Confirm Password Field */}
              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirm Password</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className={errors.confirmPassword ? "border-red-500" : ""}
                  required
                />
                {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>}
                
                {showValidation && formData.confirmPassword && (
                  <div className="text-xs space-y-1 mt-1">
                    <div className="flex items-center">
                      {renderCriteriaIcon(validateConfirmPassword(formData.password, formData.confirmPassword))}
                      <span className="ml-2">Passwords match</span>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
            <CardFooter className="flex flex-col space-y-4">
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? "Creating account..." : "Register"}
              </Button>
              <div className="text-center text-sm text-muted-foreground">
                Already have an account?{" "}
                <Link href="/login" className="font-medium text-primary hover:underline">
                  Login
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