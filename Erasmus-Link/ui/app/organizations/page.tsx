"use client"

import { useState } from "react"
import Link from "next/link"
import { Header } from "@/components/header"
import { Footer } from "@/components/footer"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Star, MapPin, Filter, CheckCircle } from "lucide-react"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import { Label } from "@/components/ui/label"
import { Slider } from "@/components/ui/slider"
import { Checkbox } from "@/components/ui/checkbox"

// Sample organizations data
const organizations = [
  {
    id: 1,
    name: "University of Barcelona",
    type: "University",
    location: "Barcelona, Spain",
    verified: true,
    rating: 4.8,
    reviews: 24,
    description: "One of Spain's leading universities with a strong focus on international exchange programs.",
    programCount: 12,
    tags: ["Higher Education", "Research", "Cultural Exchange"],
    logo: "/placeholder.svg?height=80&width=80",
  },
  {
    id: 2,
    name: "Berlin Institute of Technology",
    type: "University",
    location: "Berlin, Germany",
    verified: true,
    rating: 4.5,
    reviews: 18,
    description: "A prestigious technical university offering innovative programs in engineering and technology.",
    programCount: 8,
    tags: ["Technical Education", "Engineering", "Innovation"],
    logo: "/placeholder.svg?height=80&width=80",
  },
  {
    id: 3,
    name: "University of Helsinki",
    type: "University",
    location: "Helsinki, Finland",
    verified: true,
    rating: 4.9,
    reviews: 32,
    description: "Finland's leading research university with excellent digital education programs.",
    programCount: 15,
    tags: ["Research", "Digital Skills", "Nordic Studies"],
    logo: "/placeholder.svg?height=80&width=80",
  },
  {
    id: 4,
    name: "University of Copenhagen",
    type: "University",
    location: "Copenhagen, Denmark",
    verified: true,
    rating: 4.7,
    reviews: 15,
    description: "One of the oldest universities in Northern Europe with a strong focus on sustainability.",
    programCount: 10,
    tags: ["Sustainability", "Research", "Nordic Studies"],
    logo: "/placeholder.svg?height=80&width=80",
  },
  {
    id: 5,
    name: "Lisbon Business School",
    type: "Business School",
    location: "Lisbon, Portugal",
    verified: true,
    rating: 4.6,
    reviews: 27,
    description: "A leading business school offering entrepreneurship and management programs.",
    programCount: 6,
    tags: ["Business", "Entrepreneurship", "Management"],
    logo: "/placeholder.svg?height=80&width=80",
  },
  {
    id: 6,
    name: "Academy of Fine Arts Vienna",
    type: "Art School",
    location: "Vienna, Austria",
    verified: true,
    rating: 4.4,
    reviews: 19,
    description: "One of Europe's oldest art schools with a rich tradition in fine arts education.",
    programCount: 5,
    tags: ["Arts", "Culture", "Creative"],
    logo: "/placeholder.svg?height=80&width=80",
  },
  {
    id: 7,
    name: "Paris Institute of Political Studies",
    type: "University",
    location: "Paris, France",
    verified: true,
    rating: 4.7,
    reviews: 22,
    description: "A prestigious institution specializing in social sciences, international relations, and politics.",
    programCount: 9,
    tags: ["Political Science", "International Relations", "Social Sciences"],
    logo: "/placeholder.svg?height=80&width=80",
  },
  {
    id: 8,
    name: "Technical University of Milan",
    type: "University",
    location: "Milan, Italy",
    verified: true,
    rating: 4.5,
    reviews: 16,
    description: "Italy's largest technical university with strong programs in engineering and design.",
    programCount: 11,
    tags: ["Engineering", "Design", "Architecture"],
    logo: "/placeholder.svg?height=80&width=80",
  },
  {
    id: 9,
    name: "Stockholm School of Economics",
    type: "Business School",
    location: "Stockholm, Sweden",
    verified: true,
    rating: 4.8,
    reviews: 20,
    description: "A leading business school in Northern Europe with a focus on sustainable business practices.",
    programCount: 7,
    tags: ["Business", "Economics", "Sustainability"],
    logo: "/placeholder.svg?height=80&width=80",
  },
]

export default function OrganizationsPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedType, setSelectedType] = useState("")
  const [minRating, setMinRating] = useState(0)
  const [filteredOrganizations, setFilteredOrganizations] = useState(organizations)

  const handleSearch = () => {
    const filtered = organizations.filter((org) => {
      const matchesSearch =
        org.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        org.location.toLowerCase().includes(searchTerm.toLowerCase()) ||
        org.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        org.tags.some((tag) => tag.toLowerCase().includes(searchTerm.toLowerCase()))

      const matchesType = selectedType ? org.type === selectedType : true

      const matchesRating = org.rating >= minRating

      return matchesSearch && matchesType && matchesRating
    })

    setFilteredOrganizations(filtered)
  }

  const resetFilters = () => {
    setSearchTerm("")
    setSelectedType("")
    setMinRating(0)
    setFilteredOrganizations(organizations)
  }

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        <section className="bg-muted/50 py-8">
          <div className="container px-4 md:px-6">
            <div className="flex flex-col space-y-4">
              <h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl">Erasmus+ Organizations</h1>
              <p className="max-w-[700px] text-muted-foreground md:text-xl">
                Find and connect with universities and organizations participating in Erasmus+ programs
              </p>
              <div className="flex flex-col space-y-4 md:flex-row md:space-x-4 md:space-y-0">
                <div className="flex-1">
                  <Input
                    placeholder="Search organizations, locations, or specialties..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full"
                  />
                </div>
                <Sheet>
                  <SheetTrigger asChild>
                    <Button variant="outline" className="flex items-center gap-2">
                      <Filter className="h-4 w-4" />
                      Filters
                    </Button>
                  </SheetTrigger>
                  <SheetContent>
                    <SheetHeader>
                      <SheetTitle>Filter Organizations</SheetTitle>
                      <SheetDescription>Refine your search with these filters</SheetDescription>
                    </SheetHeader>
                    <div className="grid gap-4 py-4">
                      <div className="space-y-2">
                        <Label htmlFor="organization-type">Organization Type</Label>
                        <Select value={selectedType} onValueChange={setSelectedType}>
                          <SelectTrigger id="organization-type">
                            <SelectValue placeholder="All Types" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="all">All Types</SelectItem>
                            <SelectItem value="University">University</SelectItem>
                            <SelectItem value="Business School">Business School</SelectItem>
                            <SelectItem value="Art School">Art School</SelectItem>
                            <SelectItem value="NGO">NGO</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label>Minimum Rating</Label>
                        <div className="flex items-center space-x-2">
                          <Slider
                            defaultValue={[0]}
                            max={5}
                            step={0.5}
                            value={[minRating]}
                            onValueChange={(value) => setMinRating(value[0])}
                          />
                          <span className="w-12 text-center">{minRating.toFixed(1)}</span>
                        </div>
                      </div>
                      <div className="space-y-2">
                        <Label>Verification Status</Label>
                        <div className="flex items-center space-x-2">
                          <Checkbox id="verified-only" />
                          <label
                            htmlFor="verified-only"
                            className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                          >
                            Show only verified organizations
                          </label>
                        </div>
                      </div>
                    </div>
                    <div className="flex justify-between">
                      <Button variant="outline" onClick={resetFilters}>
                        Reset
                      </Button>
                      <Button onClick={handleSearch}>Apply Filters</Button>
                    </div>
                  </SheetContent>
                </Sheet>
                <Button onClick={handleSearch}>Search</Button>
              </div>
            </div>
          </div>
        </section>

        <section className="py-8">
          <div className="container px-4 md:px-6">
            <Tabs defaultValue="all" className="w-full">
              <TabsList className="mb-4">
                <TabsTrigger value="all">All Organizations</TabsTrigger>
                <TabsTrigger value="universities">Universities</TabsTrigger>
                <TabsTrigger value="business">Business Schools</TabsTrigger>
                <TabsTrigger value="other">Other Institutions</TabsTrigger>
              </TabsList>
              <TabsContent value="all" className="space-y-4">
                {filteredOrganizations.length > 0 ? (
                  <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {filteredOrganizations.map((org) => (
                      <Link href={`/organizations/${org.id}`} key={org.id}>
                        <Card className="h-full overflow-hidden transition-all hover:shadow-md">
                          <CardHeader className="pb-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center gap-4">
                                <Avatar className="h-12 w-12">
                                  <AvatarImage src={org.logo || "/placeholder.svg"} alt={org.name} />
                                  <AvatarFallback>
                                    {org.name
                                      .split(" ")
                                      .map((n) => n[0])
                                      .join("")}
                                  </AvatarFallback>
                                </Avatar>
                                <div>
                                  <CardTitle className="flex items-center gap-2 text-lg">
                                    {org.name}
                                    {org.verified && <CheckCircle className="h-4 w-4 text-green-500" />}
                                  </CardTitle>
                                  <CardDescription>{org.type}</CardDescription>
                                </div>
                              </div>
                            </div>
                          </CardHeader>
                          <CardContent className="pb-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center text-sm text-muted-foreground">
                                <MapPin className="mr-1 h-4 w-4" />
                                {org.location}
                              </div>
                              <div className="flex items-center">
                                <Star className="mr-1 h-4 w-4 fill-primary text-primary" />
                                <span className="text-sm font-medium">
                                  {org.rating} ({org.reviews})
                                </span>
                              </div>
                            </div>
                            <p className="mt-2 line-clamp-2 text-sm">{org.description}</p>
                            <div className="mt-2 text-sm text-muted-foreground">{org.programCount} active programs</div>
                          </CardContent>
                          <CardFooter className="flex flex-wrap gap-1">
                            {org.tags.slice(0, 3).map((tag) => (
                              <Badge key={tag} variant="outline" className="text-xs">
                                {tag}
                              </Badge>
                            ))}
                          </CardFooter>
                        </Card>
                      </Link>
                    ))}
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center py-12 text-center">
                    <p className="text-lg font-medium">No organizations found matching your criteria</p>
                    <p className="text-muted-foreground">Try adjusting your filters or search term</p>
                    <Button variant="outline" className="mt-4" onClick={resetFilters}>
                      Reset Filters
                    </Button>
                  </div>
                )}
              </TabsContent>
              <TabsContent value="universities" className="space-y-4">
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {organizations
                    .filter((org) => org.type === "University")
                    .map((org) => (
                      <Link href={`/organizations/${org.id}`} key={org.id}>
                        <Card className="h-full overflow-hidden transition-all hover:shadow-md">
                          <CardHeader className="pb-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center gap-4">
                                <Avatar className="h-12 w-12">
                                  <AvatarImage src={org.logo || "/placeholder.svg"} alt={org.name} />
                                  <AvatarFallback>
                                    {org.name
                                      .split(" ")
                                      .map((n) => n[0])
                                      .join("")}
                                  </AvatarFallback>
                                </Avatar>
                                <div>
                                  <CardTitle className="flex items-center gap-2 text-lg">
                                    {org.name}
                                    {org.verified && <CheckCircle className="h-4 w-4 text-green-500" />}
                                  </CardTitle>
                                  <CardDescription>{org.type}</CardDescription>
                                </div>
                              </div>
                            </div>
                          </CardHeader>
                          <CardContent className="pb-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center text-sm text-muted-foreground">
                                <MapPin className="mr-1 h-4 w-4" />
                                {org.location}
                              </div>
                              <div className="flex items-center">
                                <Star className="mr-1 h-4 w-4 fill-primary text-primary" />
                                <span className="text-sm font-medium">
                                  {org.rating} ({org.reviews})
                                </span>
                              </div>
                            </div>
                            <p className="mt-2 line-clamp-2 text-sm">{org.description}</p>
                            <div className="mt-2 text-sm text-muted-foreground">{org.programCount} active programs</div>
                          </CardContent>
                          <CardFooter className="flex flex-wrap gap-1">
                            {org.tags.slice(0, 3).map((tag) => (
                              <Badge key={tag} variant="outline" className="text-xs">
                                {tag}
                              </Badge>
                            ))}
                          </CardFooter>
                        </Card>
                      </Link>
                    ))}
                </div>
              </TabsContent>
              <TabsContent value="business" className="space-y-4">
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {organizations
                    .filter((org) => org.type === "Business School")
                    .map((org) => (
                      <Link href={`/organizations/${org.id}`} key={org.id}>
                        <Card className="h-full overflow-hidden transition-all hover:shadow-md">
                          <CardHeader className="pb-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center gap-4">
                                <Avatar className="h-12 w-12">
                                  <AvatarImage src={org.logo || "/placeholder.svg"} alt={org.name} />
                                  <AvatarFallback>
                                    {org.name
                                      .split(" ")
                                      .map((n) => n[0])
                                      .join("")}
                                  </AvatarFallback>
                                </Avatar>
                                <div>
                                  <CardTitle className="flex items-center gap-2 text-lg">
                                    {org.name}
                                    {org.verified && <CheckCircle className="h-4 w-4 text-green-500" />}
                                  </CardTitle>
                                  <CardDescription>{org.type}</CardDescription>
                                </div>
                              </div>
                            </div>
                          </CardHeader>
                          <CardContent className="pb-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center text-sm text-muted-foreground">
                                <MapPin className="mr-1 h-4 w-4" />
                                {org.location}
                              </div>
                              <div className="flex items-center">
                                <Star className="mr-1 h-4 w-4 fill-primary text-primary" />
                                <span className="text-sm font-medium">
                                  {org.rating} ({org.reviews})
                                </span>
                              </div>
                            </div>
                            <p className="mt-2 line-clamp-2 text-sm">{org.description}</p>
                            <div className="mt-2 text-sm text-muted-foreground">{org.programCount} active programs</div>
                          </CardContent>
                          <CardFooter className="flex flex-wrap gap-1">
                            {org.tags.slice(0, 3).map((tag) => (
                              <Badge key={tag} variant="outline" className="text-xs">
                                {tag}
                              </Badge>
                            ))}
                          </CardFooter>
                        </Card>
                      </Link>
                    ))}
                </div>
              </TabsContent>
              <TabsContent value="other" className="space-y-4">
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {organizations
                    .filter((org) => org.type !== "University" && org.type !== "Business School")
                    .map((org) => (
                      <Link href={`/organizations/${org.id}`} key={org.id}>
                        <Card className="h-full overflow-hidden transition-all hover:shadow-md">
                          <CardHeader className="pb-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center gap-4">
                                <Avatar className="h-12 w-12">
                                  <AvatarImage src={org.logo || "/placeholder.svg"} alt={org.name} />
                                  <AvatarFallback>
                                    {org.name
                                      .split(" ")
                                      .map((n) => n[0])
                                      .join("")}
                                  </AvatarFallback>
                                </Avatar>
                                <div>
                                  <CardTitle className="flex items-center gap-2 text-lg">
                                    {org.name}
                                    {org.verified && <CheckCircle className="h-4 w-4 text-green-500" />}
                                  </CardTitle>
                                  <CardDescription>{org.type}</CardDescription>
                                </div>
                              </div>
                            </div>
                          </CardHeader>
                          <CardContent className="pb-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center text-sm text-muted-foreground">
                                <MapPin className="mr-1 h-4 w-4" />
                                {org.location}
                              </div>
                              <div className="flex items-center">
                                <Star className="mr-1 h-4 w-4 fill-primary text-primary" />
                                <span className="text-sm font-medium">
                                  {org.rating} ({org.reviews})
                                </span>
                              </div>
                            </div>
                            <p className="mt-2 line-clamp-2 text-sm">{org.description}</p>
                            <div className="mt-2 text-sm text-muted-foreground">{org.programCount} active programs</div>
                          </CardContent>
                          <CardFooter className="flex flex-wrap gap-1">
                            {org.tags.slice(0, 3).map((tag) => (
                              <Badge key={tag} variant="outline" className="text-xs">
                                {tag}
                              </Badge>
                            ))}
                          </CardFooter>
                        </Card>
                      </Link>
                    ))}
                </div>
              </TabsContent>
            </Tabs>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  )
}
