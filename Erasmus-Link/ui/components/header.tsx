"use client"

import Link from "next/link"
import { useState, useEffect } from "react"
import { usePathname, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  NavigationMenu,
  NavigationMenuContent,
  NavigationMenuItem,
  NavigationMenuLink,
  NavigationMenuList,
  NavigationMenuTrigger,
  navigationMenuTriggerStyle,
} from "@/components/ui/navigation-menu"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { 
  Menu, 
  Search, 
  X, 
  User,
  LogOut,
  Shield
} from "lucide-react"
import { ModeToggle } from "@/components/mode-toggle"
import { 
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { getUser, logout, type User as UserType, formatUserType } from "@/lib/auth-utils"

export function Header() {
  const [showSearch, setShowSearch] = useState(false)
  
  const [user, setUser] = useState<UserType | null>(null)
  const [isAdmin, setIsAdmin] = useState(false)
  
  const pathname = usePathname()
  const router = useRouter()

  // Update user data function
  const updateUserData = () => {
    const currentUser = getUser()
    setUser(currentUser)
  }

  useEffect(() => {
    // Initial load
    updateUserData()

    const currentUser = getUser()
    setUser(currentUser)
    
    // Check if user is admin
    if (currentUser && currentUser.userType === "ADMIN") {
      setIsAdmin(true)
    } else {
      setIsAdmin(false)
    }

    // Add event listener for storage changes (in case of logout in another tab)
    const handleStorageChange = (e: StorageEvent) => {
      const updatedUser = getUser()
      setUser(updatedUser)
      setIsAdmin(updatedUser?.userType === "ADMIN")
      if (e.key === 'user') {
          updateUserData()
      }
    }

    // Listen for custom events (for same-tab updates)
    const handleUserUpdate = () => {
      updateUserData()
    }
    
    window.addEventListener('storage', handleStorageChange)
    window.addEventListener('userUpdated', handleUserUpdate)

    return () => {
      window.removeEventListener('storage', handleStorageChange)
      window.removeEventListener('userUpdated', handleUserUpdate)
    }
  }, [])

  // Function to handle logout
  const handleLogout = () => {
    logout()
    setUser(null)
    setIsAdmin(false)
    router.push('/')
  }

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-16 items-center justify-between">
        <div className="flex items-center gap-2 md:gap-6">
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="md:hidden">
                <Menu className="h-5 w-5" />
                <span className="sr-only">Toggle menu</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-[300px] sm:w-[400px]">
              <nav className="flex flex-col gap-4">
                <Link
                  href="/"
                  className="text-lg font-bold tracking-tight"
                  onClick={() => {
                    const sheet = document.querySelector('[data-state="open"]')
                    if (sheet) {
                      sheet.setAttribute("data-state", "closed")
                    }
                  }}
                >
                  ErasmusLink
                </Link>
                <Link
                  href="/programs"
                  className="text-sm font-medium transition-colors hover:text-primary"
                  onClick={() => {
                    const sheet = document.querySelector('[data-state="open"]')
                    if (sheet) {
                      sheet.setAttribute("data-state", "closed")
                    }
                  }}
                >
                  Programs
                </Link>
                <Link
                  href="/organizations"
                  className="text-sm font-medium transition-colors hover:text-primary"
                  onClick={() => {
                    const sheet = document.querySelector('[data-state="open"]')
                    if (sheet) {
                      sheet.setAttribute("data-state", "closed")
                    }
                  }}
                >
                  Organizations
                </Link>
                <Link
                  href="/about"
                  className="text-sm font-medium transition-colors hover:text-primary"
                  onClick={() => {
                    const sheet = document.querySelector('[data-state="open"]')
                    if (sheet) {
                      sheet.setAttribute("data-state", "closed")
                    }
                  }}
                >
                  About
                </Link>
                <Link
                  href="/contact"
                  className="text-sm font-medium transition-colors hover:text-primary"
                  onClick={() => {
                    const sheet = document.querySelector('[data-state="open"]')
                    if (sheet) {
                      sheet.setAttribute("data-state", "closed")
                    }
                  }}
                >
                  Contact
                </Link>

                {/* Admin section for mobile */}
                {isAdmin && (
                  <>
                    <div className="text-sm font-medium text-primary-foreground bg-primary px-3 py-1 rounded-md mt-2">
                      Admin Section
                    </div>
                    <Link
                      href="/admin/users"
                      className="text-sm font-medium transition-colors hover:text-primary flex items-center gap-2"
                      onClick={() => {
                        const sheet = document.querySelector('[data-state="open"]')
                        if (sheet) {
                          sheet.setAttribute("data-state", "closed")
                        }
                      }}
                    >
                      <Shield className="h-4 w-4" />
                      Manage Users
                    </Link>
                    <Link
                      href="/admin/programs"
                      className="text-sm font-medium transition-colors hover:text-primary flex items-center gap-2"
                      onClick={() => {
                        const sheet = document.querySelector('[data-state="open"]')
                        if (sheet) {
                          sheet.setAttribute("data-state", "closed")
                        }
                      }}
                    >
                      <Shield className="h-4 w-4" />
                      Manage Programs
                    </Link>
                  </>
                )}

                {user ? (
                  <>
                    <div className="text-sm font-medium text-muted-foreground">
                      {formatUserType(user.userType)}
                    </div>
                    <Button
                      variant="ghost"
                      className="text-sm font-medium transition-colors hover:text-primary"
                      onClick={() => {
                        handleLogout()
                        const sheet = document.querySelector('[data-state="open"]')
                        if (sheet) {
                          sheet.setAttribute("data-state", "closed")
                        }
                      }}
                    >
                      Logout
                    </Button>
                  </>
                ) : (
                  <>
                    <Link
                      href="/login"
                      className="text-sm font-medium transition-colors hover:text-primary"
                      onClick={() => {
                        const sheet = document.querySelector('[data-state="open"]')
                        if (sheet) {
                          sheet.setAttribute("data-state", "closed")
                        }
                      }}
                    >
                      Login
                    </Link>
                    <Link
                      href="/register"
                      className="text-sm font-medium transition-colors hover:text-primary"
                      onClick={() => {
                        const sheet = document.querySelector('[data-state="open"]')
                        if (sheet) {
                          sheet.setAttribute("data-state", "closed")
                        }
                      }}
                    >
                      Register
                    </Link>
                  </>
                )}
              </nav>
            </SheetContent>
          </Sheet>
          <Link href="/" className="flex items-center gap-2">
            <span className="text-xl font-bold tracking-tight">ErasmusLink</span>
          </Link>
          <NavigationMenu className="hidden md:flex">
            <NavigationMenuList>
              <NavigationMenuItem>
                <NavigationMenuTrigger>Programs</NavigationMenuTrigger>
                <NavigationMenuContent>
                  <ul className="grid w-[400px] gap-3 p-4 md:w-[500px] md:grid-cols-2 lg:w-[600px]">
                    <li className="row-span-3">
                      <NavigationMenuLink asChild>
                        <a
                          className="flex h-full w-full select-none flex-col justify-end rounded-md bg-gradient-to-b from-muted/50 to-muted p-6 no-underline outline-none focus:shadow-md"
                          href="/programs"
                        >
                          <div className="mb-2 mt-4 text-lg font-medium">All Programs</div>
                          <p className="text-sm leading-tight text-muted-foreground">
                            Browse all available Erasmus+ programs
                          </p>
                        </a>
                      </NavigationMenuLink>
                    </li>
                    <li>
                      <Link
                        href="/programs/ka1"
                        className="block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground"
                      >
                        <div className="text-sm font-medium leading-none">KA1 Programs</div>
                        <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
                          Learning Mobility of Individuals
                        </p>
                      </Link>
                    </li>
                    <li>
                      <Link
                        href="/programs/ka2"
                        className="block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground"
                      >
                        <div className="text-sm font-medium leading-none">KA2 Programs</div>
                        <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
                          Cooperation for Innovation
                        </p>
                      </Link>
                    </li>
                    <li>
                      <Link
                        href="/programs/ka3"
                        className="block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground"
                      >
                        <div className="text-sm font-medium leading-none">KA3 Programs</div>
                        <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
                          Support for Policy Reform
                        </p>
                      </Link>
                    </li>
                  </ul>
                </NavigationMenuContent>
              </NavigationMenuItem>
              <NavigationMenuItem>
                <Link href="/organizations" legacyBehavior passHref>
                  <NavigationMenuLink className={navigationMenuTriggerStyle()}>Organizations</NavigationMenuLink>
                </Link>
              </NavigationMenuItem>
              <NavigationMenuItem>
                <Link href="/about" legacyBehavior passHref>
                  <NavigationMenuLink className={navigationMenuTriggerStyle()}>About</NavigationMenuLink>
                </Link>
              </NavigationMenuItem>

              {/* Admin section in main nav has been removed as requested */}
            </NavigationMenuList>
          </NavigationMenu>
        </div>
        <div className="flex items-center gap-2">
          {showSearch ? (
            <div className="flex items-center">
              <Input type="search" placeholder="Search..." className="w-[200px] md:w-[300px]" />
              <Button variant="ghost" size="icon" onClick={() => setShowSearch(false)}>
                <X className="h-5 w-5" />
                <span className="sr-only">Close search</span>
              </Button>
            </div>
          ) : (
            <Button variant="ghost" size="icon" onClick={() => setShowSearch(true)}>
              <Search className="h-5 w-5" />
              <span className="sr-only">Search</span>
            </Button>
          )}
          <ModeToggle />
          <div className="hidden md:flex md:gap-2">
            {user ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative flex items-center gap-2">
                    <User className="h-4 w-4" />
                    <span>{user.username}</span>
                    {isAdmin && <Shield className="h-3 w-3 text-primary" />}
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuLabel>
                    <div className="flex flex-col">
                      <span className="font-bold">{user.fullName}</span>
                      <span className="text-xs text-muted-foreground">{formatUserType(user.userType)}</span>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem asChild>
                    <Link href="/dashboard">Dashboard</Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link href="/profile">Profile</Link>
                  </DropdownMenuItem>

                  {/* Admin links in dropdown - kept as requested */}
                  {isAdmin && (
                    <>
                      <DropdownMenuSeparator />
                      <DropdownMenuLabel className="text-xs font-semibold text-primary">
                        Admin Actions
                      </DropdownMenuLabel>
                      <DropdownMenuItem asChild>
                        <Link href="/admin/users" className="flex items-center">
                          <Shield className="mr-2 h-4 w-4" />
                          <span>Manage Users</span>
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuItem asChild>
                        <Link href="/admin/programs" className="flex items-center">
                          <Shield className="mr-2 h-4 w-4" />
                          <span>Manage Programs</span>
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuItem asChild>
                        <Link href="/admin/organizations" className="flex items-center">
                          <Shield className="mr-2 h-4 w-4" />
                          <span>Manage Organizations</span>
                        </Link>
                      </DropdownMenuItem>
                    </>
                  )}

                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout} className="text-red-500 cursor-pointer">
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>Logout</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <>
                <Link href="/login">
                  <Button variant={pathname === "/login" ? "default" : "ghost"}>Login</Button>
                </Link>
                <Link href="/register">
                  <Button variant={pathname === "/register" ? "default" : "ghost"}>Register</Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}