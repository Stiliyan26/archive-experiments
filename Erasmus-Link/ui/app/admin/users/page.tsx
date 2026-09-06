"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { getUser } from "@/lib/auth-utils"
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Loader2, RefreshCw, Shield, Check, X, Edit2 } from "lucide-react"
import { Header } from "@/components/header"
import { toast } from "@/components/ui/use-toast"

// Define the user type based on the API response
interface User {
  username: string
  email: string
  fullName: string
  userType: string
  createdAt: string
}

interface EditableUser extends User {
  isEditing: boolean
  tempUsername: string
  tempEmail: string
  tempFullName: string
}

export default function AdminUsersPage() {
  const [users, setUsers] = useState<EditableUser[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [updateLoading, setUpdateLoading] = useState<string | null>(null)
  const [currentUsername, setCurrentUsername] = useState<string | null>(null)
  const router = useRouter()

  // Function to fetch users
  const fetchUsers = async () => {
    setLoading(true)
    setError(null)
    
    try {
      const response = await fetch('http://localhost:8080/api/users', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      })
      
      if (!response.ok) {
        throw new Error(`API request failed with status ${response.status}`)
      }
      
      const data = await response.json()
      
      // Add editing state to each user
      const editableUsers: EditableUser[] = data.map((user: User) => ({
        ...user,
        isEditing: false,
        tempUsername: user.username,
        tempEmail: user.email,
        tempFullName: user.fullName || ''
      }))
      
      setUsers(editableUsers)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred')
      console.error('Error fetching users:', err)
    } finally {
      setLoading(false)
    }
  }

  // Check if user is admin and fetch users on component mount
  useEffect(() => {
    const currentUser = getUser()
    
    // Redirect if not logged in or not admin
    if (!currentUser) {
      router.push('/login')
      return
    }
    
    if (currentUser.userType !== 'ADMIN') {
      router.push('/')
      return
    }
    
    // Store the current username for filtering later
    setCurrentUsername(currentUser.username)
    
    fetchUsers()
  }, [router])

  // Format date to be more readable
  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // Get badge color based on user type
  const getUserTypeBadgeColor = (userType: string) => {
    switch(userType) {
      case 'ADMIN':
        return 'bg-red-500 hover:bg-red-600'
      case 'ORGANIZATION':
        return 'bg-blue-500 hover:bg-blue-600'
      case 'PARTICIPANT':
        return 'bg-green-500 hover:bg-green-600'
      default:
        return 'bg-gray-500 hover:bg-gray-600'
    }
  }

  // Start editing a user
  const startEditing = (username: string) => {
    setUsers(users.map(user => {
      if (user.username === username) {
        return {
          ...user,
          isEditing: true,
          tempUsername: user.username,
          tempEmail: user.email,
          tempFullName: user.fullName || ''
        }
      }
      return { ...user, isEditing: false } // Cancel other edits
    }))
  }

  // Cancel editing
  const cancelEditing = (username: string) => {
    setUsers(users.map(user => {
      if (user.username === username) {
        return {
          ...user,
          isEditing: false,
          tempUsername: user.username,
          tempEmail: user.email,
          tempFullName: user.fullName || ''
        }
      }
      return user
    }))
  }

  // Handle input change
  const handleInputChange = (username: string, field: 'tempUsername' | 'tempEmail' | 'tempFullName', value: string) => {
    setUsers(users.map(user => {
      if (user.username === username) {
        return {
          ...user,
          [field]: value
        }
      }
      return user
    }))
  }

  // Save changes
  const saveChanges = async (username: string) => {
    const user = users.find(u => u.username === username)
    if (!user) return
    
    setUpdateLoading(username)
    
    try {
      // Call the actual API endpoint to update the user
      // Based on your UpdateUserDTO requirements
      const response = await fetch(`http://localhost:8080/api/users/${username}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: user.tempUsername,
          email: user.tempEmail,
          fullName: user.tempFullName,
          userType: user.userType // Send the original userType without modification
        })
      })
      
      if (!response.ok) {
        let errorMessage = `API request failed with status ${response.status}`;
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorData.error || errorMessage;
          console.error('Error response:', errorData);
        } catch (e) {
          // If parsing JSON fails, use the default error message
        }
        throw new Error(errorMessage);
      }
      
      const updatedUser = await response.json();
      
      // Update local state with values returned from the server
      setUsers(users.map(u => {
        if (u.username === username) {
          return {
            ...u,
            username: updatedUser.username || user.tempUsername,
            email: updatedUser.email || user.tempEmail,
            fullName: updatedUser.fullName || user.tempFullName,
            // userType remains unchanged
            isEditing: false,
            tempUsername: updatedUser.username || user.tempUsername,
            tempEmail: updatedUser.email || user.tempEmail,
            tempFullName: updatedUser.fullName || user.tempFullName
          }
        }
        return u
      }))
      
      toast({
        title: "User updated",
        description: "User information has been successfully updated",
      })
      
      // Refresh the user list to make sure we have the latest data
      fetchUsers()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred')
      console.error('Error updating user:', err)
      
      toast({
        title: "Update failed",
        description: err instanceof Error ? err.message : 'An unknown error occurred',
        variant: "destructive"
      })
    } finally {
      setUpdateLoading(null)
    }
  }

  return (
    <>
      <Header />
      <div className="container py-10">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="text-2xl flex items-center">
                <Shield className="mr-2 h-5 w-5" />
                User Management
              </CardTitle>
              <CardDescription>
                Manage all users in the ErasmusLink platform
              </CardDescription>
            </div>
            <Button variant="outline" onClick={fetchUsers} disabled={loading}>
              {loading ? (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              ) : (
                <RefreshCw className="h-4 w-4 mr-2" />
              )}
              Refresh
            </Button>
          </CardHeader>
          <CardContent>
            {error && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                Error: {error}
              </div>
            )}

            {loading ? (
              <div className="flex justify-center items-center py-10">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
              </div>
            ) : (
              <Table>
                <TableCaption>A list of all users in the system</TableCaption>
                <TableHeader>
                  <TableRow>
                    <TableHead>Username</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Full Name</TableHead>
                    <TableHead>User Type</TableHead>
                    <TableHead>Created At</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {users.length === 0 || users.filter(user => user.username !== currentUsername).length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center py-10 text-muted-foreground">
                        No users found
                      </TableCell>
                    </TableRow>
                  ) : (
                    users
                      .filter(user => user.username !== currentUsername) // Filter out the current user
                      .map((user) => (
                      <TableRow key={user.username}>
                        <TableCell className="font-medium">
                          {user.isEditing ? (
                            <Input 
                              value={user.tempUsername} 
                              onChange={(e) => handleInputChange(user.username, 'tempUsername', e.target.value)}
                              className="max-w-[200px]"
                            />
                          ) : (
                            user.username
                          )}
                        </TableCell>
                        <TableCell>
                          {user.isEditing ? (
                            <Input 
                              value={user.tempEmail} 
                              onChange={(e) => handleInputChange(user.username, 'tempEmail', e.target.value)}
                              className="max-w-[200px]"
                            />
                          ) : (
                            user.email
                          )}
                        </TableCell>
                        <TableCell>
                          {user.isEditing ? (
                            <Input 
                              value={user.tempFullName} 
                              onChange={(e) => handleInputChange(user.username, 'tempFullName', e.target.value)}
                              className="max-w-[200px]"
                            />
                          ) : (
                            user.fullName
                          )}
                        </TableCell>
                        <TableCell>
                          <Badge className={getUserTypeBadgeColor(user.userType)}>
                            {user.userType}
                          </Badge>
                          {user.isEditing && (
                            <input 
                              type="hidden" 
                              value={user.userType} 
                              // Hidden input to ensure userType is sent with the form
                            />
                          )}
                        </TableCell>
                        <TableCell>{formatDate(user.createdAt)}</TableCell>
                        <TableCell className="text-right">
                          {user.isEditing ? (
                            <div className="flex justify-end gap-2">
                              <Button 
                                variant="ghost" 
                                size="sm" 
                                onClick={() => saveChanges(user.username)}
                                disabled={updateLoading === user.username}
                              >
                                {updateLoading === user.username ? (
                                  <Loader2 className="h-4 w-4 animate-spin" />
                                ) : (
                                  <Check className="h-4 w-4 text-green-500" />
                                )}
                              </Button>
                              <Button 
                                variant="ghost" 
                                size="sm" 
                                onClick={() => cancelEditing(user.username)}
                                disabled={updateLoading === user.username}
                              >
                                <X className="h-4 w-4 text-red-500" />
                              </Button>
                            </div>
                          ) : (
                            <Button 
                              variant="ghost" 
                              size="sm" 
                              onClick={() => startEditing(user.username)}
                            >
                              <Edit2 className="h-4 w-4 mr-1" /> Edit
                            </Button>
                          )}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  )
}