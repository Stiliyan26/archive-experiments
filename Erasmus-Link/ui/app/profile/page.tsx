"use client"

import { useState, useEffect } from "react"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import { AlertCircle, Check, X, Pencil } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"
import { Badge } from "@/components/ui/badge"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"

interface User {
    fullName: string
    username: string
    email: string
    userType: "ADMIN" | "ORGANIZATION" | "PARTICIPANT"
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
}

export default function UserProfilePage() {
    const router = useRouter()
    const [userData, setUserData] = useState<User | null>(null)
    const [originalData, setOriginalData] = useState<User | null>(null)
    const [editMode, setEditMode] = useState(false)
    const [formError, setFormError] = useState("")
    const [error, setError] = useState<string>("")
    const [loading, setLoading] = useState<boolean>(true)
    const [parsedUser, setParsedUser] = useState<any>(null)
    const [showValidation, setShowValidation] = useState<boolean>(false)

    const [validation, setValidation] = useState<ValidationState>({
        fullName: {
            valid: true,
            message: "",
            criteria: {
                minLength: false,
                format: false
            }
        },
        username: {
            valid: true,
            message: "",
            criteria: {
                minLength: false,
                format: false,
                noSpaces: false
            }
        },
        email: {
            valid: true,
            message: "",
            criteria: {
                format: false
            }
        }
    })

    useEffect(() => {
        const storedUser = sessionStorage.getItem("user")
        if (!storedUser) {
            setLoading(false)
            router.push('/login')
            return
        }

        const parsed = JSON.parse(storedUser)
        setParsedUser(parsed)

        const fetchUser = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/users/${parsed.username}`)
                if (!response.ok) throw new Error("Failed to fetch user data.")
                const data = await response.json()
                setUserData(data)
                setOriginalData(data)
                // Validate initial data
                validateFullName(data.fullName)
                validateUsername(data.username)
                validateEmail(data.email)
            } catch (err) {
                setError("Failed to load user data.")
            } finally {
                setLoading(false)
            }
        }

        fetchUser()
    }, [])

    // Validation functions
    const validateFullName = (fullName: string) => {
        const hasValidLength = fullName.trim().length >= 2
        const hasValidFormat = /^[A-Za-zА-Яа-я\s'-]+$/.test(fullName)

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

    const validateForm = (): boolean => {
        if (!userData) return false

        const fullNameValid = validateFullName(userData.fullName)
        const usernameValid = validateUsername(userData.username)
        const emailValid = validateEmail(userData.email)

        setShowValidation(true)
        return fullNameValid && usernameValid && emailValid
    }

    const handleEditToggle = () => {
        setEditMode(true)
        setShowValidation(false)
    }

    const handleCancel = () => {
        if (originalData) {
            setUserData({ ...originalData })
            // Reset validation to original state
            validateFullName(originalData.fullName)
            validateUsername(originalData.username)
            validateEmail(originalData.email)
        }
        setEditMode(false)
        setShowValidation(false)
        if (formError) {
            setFormError("")
        }
    }

    const handleConfirm = async () => {
        if (!userData || !validateForm()) {
            return
        }

        try {
            const response = await fetch(`http://localhost:8080/api/users/${parsedUser.username}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(userData)
            })

            if (!response.ok) {
                const errorData = await response.json()
                throw new Error(errorData.message || 'Error when updating User!')
            }   

            const updated = await response.json()
            setUserData(updated)
            setOriginalData(updated)
            setEditMode(false)
            setShowValidation(false)
    
            sessionStorage.setItem("user", JSON.stringify(updated))
            setParsedUser(updated)
    
            // Dispatch custom event to notify header
            window.dispatchEvent(new CustomEvent('userUpdated'))
            
        } catch (err) {
            let errorMessage = err instanceof Error ? err.message : 'An error occured during update!'
            setFormError(errorMessage)
        }
    }

    const handleChange = (field: keyof User, value: string) => {
        if (userData) {
            setUserData({ ...userData, [field]: value })
            // Validate on change
            switch (field) {
                case 'fullName':
                    validateFullName(value)
                    break
                case 'username':
                    validateUsername(value)
                    break
                case 'email':
                    validateEmail(value)
                    break
            }
        }
        // Clear form error when user starts typing after a failed attempt
        if (formError) {
            setFormError("")
        }
    }

    // Helper function to render validation status icon
    const renderCriteriaIcon = (isValid: boolean) => {
        return isValid
            ? <Check className="h-4 w-4 text-green-500" />
            : <X className="h-4 w-4 text-red-500" />
    }

    if (loading) {
        return (
            <div className="flex justify-center py-10">
                <Card className="w-full max-w-md p-6 space-y-4">
                    <Skeleton className="h-6 w-3/4" />
                    <Skeleton className="h-6 w-1/2" />
                    <Skeleton className="h-6 w-2/3" />
                    <Skeleton className="h-6 w-1/4" />
                </Card>
            </div>
        )
    }

    if (error) {
        return (
            <div className="flex justify-center py-10">
                <Alert variant="destructive" className="w-full max-w-md">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            </div>
        )
    }

    if (!userData) return null

    return (
        <div className="flex min-h-screen flex-col">
            <Header />
            <main className="flex-1 flex items-center justify-center py-12">
                <Card className="w-full max-w-md relative">
                    <CardHeader className="flex flex-row justify-between items-center">
                        <CardTitle>User Profile:</CardTitle>
                        {editMode ? (
                            <div className="flex space-x-2">
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    onClick={handleConfirm}
                                    disabled={!validation.fullName.valid || !validation.username.valid || !validation.email.valid}
                                >
                                    <Check className="h-5 w-5 text-green-600" />
                                </Button>
                                <Button variant="ghost" size="icon" onClick={handleCancel}>
                                    <X className="h-5 w-5 text-red-600" />
                                </Button>
                            </div>
                        ) : (
                            <Button variant="ghost" size="icon" onClick={handleEditToggle}>
                                <Pencil className="h-5 w-5" />
                            </Button>
                        )}
                    </CardHeader>
                    <CardContent className="space-y-4 text-sm text-gray-700">
                        {/* Full Name Field */}
                        <div className="space-y-2">
                            {formError && (
                                <Alert variant="destructive">
                                    <AlertCircle className="h-4 w-4" />
                                    <AlertDescription>{formError}</AlertDescription>
                                </Alert>
                            )}
                            <strong>Full Name:</strong>
                            {editMode ? (
                                <>
                                    <Input
                                        value={userData.fullName}
                                        onChange={(e) => handleChange("fullName", e.target.value)}
                                        className={!validation.fullName.valid && showValidation ? "border-red-500" : ""}
                                        onFocus={() => setShowValidation(true)}
                                    />
                                    {showValidation && (
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
                                    {!validation.fullName.valid && showValidation && (
                                        <p className="text-red-500 text-xs mt-1">{validation.fullName.message}</p>
                                    )}
                                </>
                            ) : (
                                <p>{userData.fullName}</p>
                            )}
                        </div>

                        {/* Username Field */}
                        <div className="space-y-2">
                            <strong>Username:</strong>
                            {editMode ? (
                                <>
                                    <Input
                                        value={userData.username}
                                        onChange={(e) => handleChange("username", e.target.value)}
                                        className={!validation.username.valid && showValidation ? "border-red-500" : ""}
                                        onFocus={() => setShowValidation(true)}
                                    />
                                    {showValidation && (
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
                                    {!validation.username.valid && showValidation && (
                                        <p className="text-red-500 text-xs mt-1">{validation.username.message}</p>
                                    )}
                                </>
                            ) : (
                                <p>{userData.username}</p>
                            )}
                        </div>

                        {/* Email Field */}
                        <div className="space-y-2">
                            <strong>Email:</strong>
                            {editMode ? (
                                <>
                                    <Input
                                        value={userData.email}
                                        onChange={(e) => handleChange("email", e.target.value)}
                                        className={!validation.email.valid && showValidation ? "border-red-500" : ""}
                                        onFocus={() => setShowValidation(true)}
                                    />
                                    {showValidation && (
                                        <div className="text-xs space-y-1 mt-1">
                                            <div className="flex items-center">
                                                {renderCriteriaIcon(validation.email.criteria.format)}
                                                <span className="ml-2">Valid email format (example@domain.com)</span>
                                            </div>
                                        </div>
                                    )}
                                    {!validation.email.valid && showValidation && (
                                        <p className="text-red-500 text-xs mt-1">{validation.email.message}</p>
                                    )}
                                </>
                            ) : (
                                <p>{userData.email}</p>
                            )}
                        </div>

                        {/* User Type (non-editable) */}
                        <div>
                            <strong>User Type:</strong>{" "}
                            <Badge variant="outline">{userData.userType}</Badge>
                        </div>
                    </CardContent>
                </Card>
            </main>
            <Footer />
        </div>
    )
}